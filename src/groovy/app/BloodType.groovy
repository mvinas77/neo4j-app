package app

enum BloodType {

    ZERO_NEGATIVE('0-'),
    ZERO_POSITIVE('0+'),
    A_NEGATIVE('A-'),
    A_POSITIVE('A+'),
    B_NEGATIVE('B-'),
    B_POSITIVE('B+'),
    AB_NEGATIVE('AB-'),
    AB_POSITIVE('AB+')

   String simpleName

    public BloodType(String simpleName) {
        this.simpleName = simpleName
    }
}
