import web.WebServiceApplication;

public class Main {

    public static void main(String[] args) {
        try {
            /* do somethings */
            WebServiceApplication app = new WebServiceApplication();
            WebServiceApplication.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}