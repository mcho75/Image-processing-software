package ImageApp.data;


public sealed interface Transaction {
    record Modified(int index, byte[] oldLayer, byte[] newLayer) implements Transaction {}
    record Deleted(int index, byte[] layer) implements Transaction {}
    record Added(int index, byte[] newLayer) implements Transaction {}
    record MovedUp(int index) implements Transaction {}
}
