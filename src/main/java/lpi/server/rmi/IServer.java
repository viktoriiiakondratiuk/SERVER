package lpi.server.rmi;

import java.io.File;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {
    public interface Task<T> {
        T execute();
    }

    <T> T executeTask(Task<T> t) throws RemoteException;
    public static class Algoritm implements Task <byte[]>, Serializable {
        private Integer[] file_to_sort;
        int t = 0;
        public Algoritm(File file_sort) throws IOException {
            b_1 = Files.readAllBytes(file_sort.toPath());
            String St = new String(b_1);
            String[] St_1 = St.split(" ");
            Mass_to_sort = new Integer[St_1.length];
            int i=0;
            while(i <=St_1.length-1) {
                Mass_to_sort[i] = Integer.parseInt(St_1[i]);
                i++;
            }
            System.out.println(Mass_to_sort.toString());
        }
        private byte[] b_1;
        private Integer[] Mass_to_sort;


        @Override
        public byte[] execute() {
            String buff1;
            byte[] buff2;
            Sort(Mass_to_sort.length);
            StringBuffer buff = new StringBuffer();
            int i=0;
            while(i <= Mass_to_sort.length-1) {
                buff.append(Mass_to_sort[i]);
                if (i!=(Mass_to_sort.length-1)) {
                    buff.append(" ");
                }
                i++;
            }
            buff1=buff.toString();
            buff2=buff1.getBytes();
            return buff2;
        }

        private void Sort(int len) {
            int k=1;
            while(k < len)
            {
                int v = Mass_to_sort[k];
                int i= k;
                while ((i > 0) && (Mass_to_sort[i - 1] > v))
                {
                    Mass_to_sort[i] = Mass_to_sort[i - 1];
                    i--;
                }
                Mass_to_sort[i] = v;
                k++;
            }

        }
    }

    public void ping() throws RemoteException;
    public String echo(String text) throws RemoteException;

    /**
     * @throws RemoteException
     *             in case of communication issues.
     *             if the provided password does not match to the login that was
     *             already provided before.
     * @throws ArgumentException
     *             if the specified <b>login</b> or <b>password</b> are not
     *             valid.
     * @throws ServerException
     *             if the server failed to process the request.
     */


    public void exit(String sessionId) throws RemoteException, ArgumentException, ServerException;


    public static class ServerException extends RemoteException {
        private static final long serialVersionUID = 2592458695363000913L;

        public ServerException() {
            super();
        }

        public ServerException(String message, Throwable cause) {
            super(message, cause);
        }

        public ServerException(String message) {
            super(message);
        }
    }



    public static class ArgumentException extends RemoteException {
        private static final long serialVersionUID = 8404607085051949404L;

        private String argumentName;

        public ArgumentException() {
            super();
        }

        public ArgumentException(String argumentName, String message, Throwable cause) {
            super(message, cause);
            this.argumentName = argumentName;
        }

        public ArgumentException(String argumentName, String message) {
            super(message);
            this.argumentName = argumentName;
        }

        public String getArgumentName() {
            return argumentName;
        }
    }
}
