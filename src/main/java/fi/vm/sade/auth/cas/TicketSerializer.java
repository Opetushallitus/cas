package fi.vm.sade.auth.cas;

public interface TicketSerializer {

    String toJson(Object object);

    <T> T fromJson(String json, Class<T> type);

}
