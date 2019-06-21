import org.apache.commons.lang3.RandomStringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Generator {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    static String generateString(int minLength, int maxLength) {
        int length = generateInt(minLength, maxLength);
        return RandomStringUtils.randomAlphabetic(length);
    }

    static String generateDate(String before, String after) {
        long offset = Timestamp.valueOf(before).getTime();
        long end = Timestamp.valueOf(after).getTime();
        long diff = end - offset + 1;
        Date randomDate = new Date(offset + (long)(Math.random() * diff));
        return dateFormat.format(randomDate).substring(0, 16);
    }

    static int generateInt(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    static String generateEnum(){
        Random r = new Random();
        String position = "";
        switch (r.nextInt(4)) {
            case 0:
                position = "Junior Manager";
                break;
            case 1:
                position = "Senior Manager";
                break;
            case 2:
                position = "Administrator";
                break;
            case 3:
                position = "Storage Worker";
                break;
             default:
                 break;
        }
        return position;
    }

    static String generatePhoneNumber(){
        int num1, num2, num3;
        int set2, set3;

        Random generator = new Random();

        num1 = generator.nextInt(7) + 1;
        num2 = generator.nextInt(8);
        num3 = generator.nextInt(8);

        set2 = generator.nextInt(643) + 100;

        set3 = generator.nextInt(8999) + 1000;

        return  "+7 (" + num1 + "" + num2 + "" + num3 + ")" + "-" + set2 + "-" + set3;
    }
}
