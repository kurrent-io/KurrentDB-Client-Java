package io.kurrent.dbclient.samples;

public class TestEvent {
    private String id;
    private String importantData;

    public TestEvent(){
    }

    public TestEvent(String id, String importantData){
        this.id = id;
        this.importantData = importantData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getImportantData() {
        return importantData;
    }

    public void setImportantData(String importantData){
        this.importantData = importantData;
    }
}
