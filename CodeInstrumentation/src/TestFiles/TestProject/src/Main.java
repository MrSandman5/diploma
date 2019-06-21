public class Main {

    public static void main(String[] args) {
        int max = 1000;
        switch (max){
            case 1000:
                max = 0;
                break;
            case 0:
                max = 1000;
                break;
        }
    }

    public static void wake(){
        int max = 1000;
        switch (max){
            case 1000:
                max = 0;
                break;
            case 0:
                max = 1000;
                break;
        }
    }

    public static String sleep(){
        for (int i = 0; i < 10; i++){
            System.out.println(i);
        }
        return "sleep";
    }
}
