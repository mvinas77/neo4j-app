package app.search

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import grails.transaction.Transactional
import grails.validation.ValidationException

import app.Address
import app.BloodType
import app.Person
import app.Status
import search.PersonReindexProcess

@Transactional
class PersonsService {

    def searchService
    def neo4jDatastore

    private static final List cities = ['New York', 'Miami', 'Austin', 'San Francisco', 'Washington', 'Tokio', 'London', 'Paris', 'Buenos Aires', 'Madrid',
                                        'Berlin', 'Moscow']

    private static final List firstNames = ['John', 'Robert', 'George', 'Julia', 'Hugo', 'Christian', 'Anna', 'Lucia', 'Guadalupe', 'Herminio', 'Lucas',
                                            'Lucio', 'Paul', 'Marcela', 'Leo', 'James', 'Michael', 'Daniel', 'Mark', 'Gary']


    private static final List lastNames = ['Smith', 'Perez', 'Rodriguez', 'Garcia', 'Johnson', 'Williams', 'Jones', 'Moore', 'Taylor', 'Hernandez', 'Martin',
                                           'White', 'Anderson', 'Jackson', 'Miller', 'Wilson', 'Brown', 'Martinez', 'Lopez']


    private static final List nationalities = ['Argentine', 'Brazilian', 'Australian', 'Belgian', 'Bolivian', 'British', 'Chinese', 'Colombian', 'Ethiopian',
                                               'Finnish', 'Dutch', 'Libyan',
                                               'Paraguayan', 'Pakistani', 'American']

    private static final List occupations = ['Employee', 'Chemical Engineer', 'Electrical Engineer', 'Industrial Engineer', 'Software Engineer',
                                             'Architect', 'Chiropractor', 'Dentist', 'Police Officer']

    private Random random = new Random(System.currentTimeMillis())

    public boolean createPersons(int batchSize = 1000) {

        int totalCreated = Person.count

        for (int i = 0; i < batchSize; i++) {

            Person person = new Person()
            person.address = new Address()
            person.address.city = pick(cities)
            person.address.number = random.nextInt(19999) + 1
            person.address.street = getStreet()
            person.address.zip = random.nextInt(999) + 1
            person.bloodType = pick(BloodType.values() as List)
            person.email = "user${totalCreated + i}@example.com"
            person.firstName = pick(firstNames)
            person.lastName = pick(lastNames)
            person.nationality = pick(nationalities)
            person.occupation = pick(occupations)
            person.phone = createPhone()
            person.status = pick(Status.values() as List)
            person.birthDate = createBirthDate()

            if (person.validate()) {
                person.save(validate: false)
            }
            else {
                throw new ValidationException('Impossible save person', person.errors)
            }

        }

    }


    public reindex() {


        log.debug("Index started ...")
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        int batchSize = 1000
        int batchCount = 0
        int processed = 0
        long startTime = System.currentTimeMillis()
        long totalCount = Person.count


        String cypher = """
            MATCH (n:Person)

            RETURN n.__id__ as id
            ORDER BY id DESC
            SKIP {skip}
            LIMIT {limit}

        """

        while (processed < totalCount) {

            long skip = batchCount * batchSize
            long limit = batchSize

            org.neo4j.cypher.javacompat.ExecutionResult result = neo4jDatastore.graphDatabaseService.execute(cypher, [skip: skip, limit: limit])

            List ids = []
            result.each { Map row ->
                ids << row.id
            }

            PersonReindexProcess process = new PersonReindexProcess(searchService, batchCount, ids)

            executorService.execute(process)

            batchCount++
            processed = processed + batchSize
        }

        long beforeCommitTime = System.currentTimeMillis()
        log.debug("Time to launch all tasks was ${System.currentTimeMillis() - beforeCommitTime}ms")
        executorService.awaitTermination(2, TimeUnit.DAYS)


        beforeCommitTime = System.currentTimeMillis()
        searchService.commit()
        long totalCommitTme = (System.currentTimeMillis() - beforeCommitTime) / 60000
        long totalTimeIndex = (beforeCommitTime - startTime) / 60000
        log.debug("=================================================================")
        log.debug("Total Index Time ${totalTimeIndex}min")
        log.debug("Total Commit Time ${totalCommitTme}min")
        log.debug("=================================================================")
    }

    private String getStreet() {

        Integer streetNumber = random.nextInt(200) + 1
        String type = pick(['Ave', 'Street'])
        return streetNumber == 1 ? "1st $type" : streetNumber == 2 ? "2nd $type" : streetNumber == 3 ? "3rd $type" : "${streetNumber}th $type"
    }

    private def pick(List options) {

        int pos = random.nextInt(options.size())
        return options.get(pos)
    }

    private String createPhone() {

        int areaCode = random.nextInt(899) + 100
        int code = random.nextInt(899) + 100
        int number = random.nextInt(9999)

        return "(${areaCode}) ${code}-${String.format("%04d", number)}"
    }

    private Date createBirthDate() {

        Integer year = 1950 + random.nextInt(45)
        Integer month = random.nextInt(11)
        Calendar calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        Integer date = random.nextInt(calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, date)

        return calendar.time
    }
}
