package app

class Person extends AbstractGraphDomain {

    Address address
    BloodType bloodType
    Date birthDate
    String email
    String firstName
    String lastName
    String phone
    String nationality
    String occupation
    Status status

    static transients = ['fullName']

    static constraints = {

        address(nullable: false)
        bloodType(nullable: false)
        email(nullable: true)
        birthDate(nullable: false)
        firstName(nullable: false)
        lastName(nullable: false)
        nationality(nullable: false)
        occupation(nullable: true)
        phone(nullable: true)
        status(nullable: false)
    }

    static mapping = {
        address lazy: false
    }

    public String getFullName() {

        StringBuffer buffer = new StringBuffer(128)

        if (firstName) {
            buffer.append(firstName)
        }

        if (lastName) {
            if (firstName) {
                buffer.append(' ')
            }
            buffer.append(lastName)
        }

        return buffer.toString()
    }

    public String toString() {

        return 'id: ' + id + ' ' + fullName
    }
}
