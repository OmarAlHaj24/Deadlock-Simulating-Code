import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static int n, m;
    private static int[] available;
    private static int[][] allocation;
    private static int[][] need;
    private static int[][] request;

    private static boolean isSafe() {
        int[] work = cloneArrayOf(available);
        boolean[] finish = new boolean[n];
        for (int i = 0; i < n; i++) {
            finish[i] = false;
        }

        for (int i = 0; i < n; i++) {
            boolean flag = false;
            if (!finish[i]) {
                flag = true;
                for (int j = 0; j < m; j++) {
                    if (need[i][j] > work[j]) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    for (int j = 0; j < m; j++) {
                        work[j] += allocation[i][j];
                    }
                    finish[i] = true;
                }
            }
            if (flag) {
                i = -1; //Return to the beginning of the loop
            }
        }

        for (int i = 0; i < n; i++) {
            if (!finish[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean banker() {
        for (int i = 0; i < n; i++) {
            boolean flag = true;
            for (int j = 0; j < m; j++) {
                if (request[i][j] <= need[i][j]) {
                    if (request[i][j] > available[j]) {
                        flag = false;
                    }
                } else {
                    System.out.println("The process has exceeded it's maximum claim!");
                    flag = false;
                }
            }
            if (flag) {
                int[][] tempAlloc = new int[allocation.length][allocation[0].length];
                int[][] tempNeed = new int[need.length][need[0].length];
                int[] tempAvail = cloneArrayOf(available);
                for (int k = 0; k < allocation.length; k++)
                    tempAlloc[k] = cloneArrayOf(allocation[k]);
                for (int k = 0; k < need.length; k++)
                    tempNeed[k] = cloneArrayOf(need[k]);
                for (int j = 0; j < m; j++) {
                    available[j] -= request[i][j];
                    allocation[i][j] += request[i][j];
                    need[i][j] -= request[i][j];
                }
                if (!isSafe()) {
                    available = tempAvail;
                    allocation = tempAlloc;
                    need = tempNeed;
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean release(int processNumber, int[] resources) {
        boolean validRelease = true;
        for (int i = 0; i < m; i++) {
            if (resources[i] > allocation[processNumber][i]) {
                validRelease = false;
                break;
            }
        }
        if (validRelease) {
            //Release the resources required to be released for each process, Increase the need, Increase the available.
            for (int i = 0; i < m; i++) {
                allocation[processNumber][i] -= resources[i];
                need[processNumber][i] += resources[i];
                available[i] += resources[i];
            }
        }
        return validRelease;
    }

    public static boolean request(int processNumber, int[] resources) {
        boolean validRequest = true;
        for (int i = 0; i < m; i++) {
            if (resources[i] > need[processNumber][i] || resources[i] > available[i]) {
                validRequest = false;
                break;
            }
        }
        if (validRequest) {
            for (int i = 0; i < m; i++) {
                allocation[processNumber][i] += resources[i];
                need[processNumber][i] -= resources[i];
                available[i] -= resources[i];
            }

        }
        return validRequest;
    }

    public static void recover() { // Victim process is picked based on maximum number of allocated resources.
        while (!isSafe()) {
            int victimIndex = 0;
            int maxSum = -1;
            for (int i = 0; i < n; i++) {
                int sum = 0;
                for (int j = 0; j < m; j++) {
                    sum += allocation[i][j];
                }
                if (sum > maxSum) {
                    maxSum = sum;
                    victimIndex = i;
                }
            }
            for (int i = 0; i < m; i++) {
                need[victimIndex][i] += allocation[victimIndex][i];
                available[i] += allocation[victimIndex][i];
                allocation[victimIndex][i] = 0;
            }
            //release(victimIndex, allocation[victimIndex]);
            System.out.printf("Process %d is picked as a victim%n", victimIndex);
        }
        System.out.println("System is safe now after applying recovery algorithm");
    }

    public static int[] cloneArrayOf(int[] arr) {
        int[] cloneArray = new int[arr.length];
        System.arraycopy(arr, 0, cloneArray, 0, arr.length);
        return cloneArray;
    }

    public static void printStats() {
        System.out.println("Current available resources: ");
        System.out.println(Arrays.toString(available));

        System.out.println("Current allocated resources: ");
        System.out.println(Arrays.deepToString(allocation));

        System.out.println("Current needed resources: ");
        System.out.println(Arrays.deepToString(need));
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of processes: ");
        n = sc.nextInt(); //Number of processes
        System.out.print("Enter number of resource types: ");
        m = sc.nextInt(); //Number of resource types
        System.out.println();
        available = new int[m];
        int[][] maximum = new int[n][m];
        allocation = new int[n][m];
        need = new int[n][m];
        request = new int[n][m];

        System.out.println("Enter available resources");
        for (int i = 0; i < m; i++) {
            available[i] = sc.nextInt();
        }

        System.out.println("Enter maximum resources");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                maximum[i][j] = sc.nextInt();
            }
        }

        System.out.println("Enter allocated resources");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                allocation[i][j] = sc.nextInt();
            }
        }

        System.out.println("Enter requested resources");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                request[i][j] = sc.nextInt();
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                need[i][j] = maximum[i][j] - allocation[i][j];
            }
        }

        if (banker()) {
            System.out.println("Banker: Requested resources granted!");
            System.out.println("Banker: System is safe, resources are allocated");
            printStats();
        } else {
            System.out.println("Banker: Requested resources denied!");
            System.out.println("Banker: System is not safe, resources are not allocated");
            printStats();
        }

        while (true) {
            String method = sc.next();
            if (method.equalsIgnoreCase("Quit"))
                break;
            else {
                int processNumber = sc.nextInt();
                int[] resources = new int[m];
                for (int i = 0; i < m; i++)
                    resources[i] = sc.nextInt();
                if (method.equals("RL")) {
                    if (release(processNumber, resources)) {
                        System.out.printf("Process %d released the specified resources successfully%n", processNumber);
                        printStats();
                    }
                    else {
                        System.out.printf("Allocation of process %d is less than targeted released resources%n", processNumber);
                        printStats();
                    }
                } else if (method.equals("RQ")) {
                    if (request(processNumber, resources)) {
                        if (isSafe()) {
                            System.out.printf("System is safe, process %d allocated resources successfully%n", processNumber);
                            printStats();
                        } else {
                            System.out.println("System is not safe, and a recovery algorithm is applied");
                            printStats();
                            recover();
                            printStats();
                        }
                    } else {
                        System.out.printf("Process %d requested more than specified maximum resources or not available amount of resources%n", processNumber);
                        printStats();
                    }
                }
            }
        }
    }
}