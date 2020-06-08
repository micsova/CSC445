package Assignment3;

public class ClientMain {
    private static Client cg;

    public static void main(String [] args) throws Exception{
        testGUI();
        cg.main();
    }

    public static boolean testGUI() {
        try{
            cg = new ClientGUI();
            //Uncomment to force a text client
            //throw new Exception();
            return true;
        } catch (Exception e) {
            cg = new ClientText();
            return false;
        }
    }
}