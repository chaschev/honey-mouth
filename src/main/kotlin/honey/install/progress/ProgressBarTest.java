package honey.install.progress;

public class ProgressBarTest {

    static public void main(String[] args) throws Exception {
        ProgressBar pb = new ProgressBar("Test", 2000, 50, System.out, ProgressBarStyle.ASCII).start();

        double x = 1.0;
        double y = x * x;

        System.out.println("\n\n\n\n\n");

        for (int i = 0; i < 10000; i++) {
            pb.step();
            Thread.sleep(1);
            if (pb.getCurrent() > 8000) pb.maxHint(10000);

        }
        pb.stop();
        System.out.println("Hello");
    }

}
