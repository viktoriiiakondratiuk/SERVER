package lpi.server.rmi;

public interface Task<T> {
    T execute();
}