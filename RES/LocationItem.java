class LocationItem {
    private String id;
    private String address;
    private String city;
    private String state;
    
    public LocationItem(String id, String address, String city, String state) {
        this.id = id;
        this.address = address;
        this.city = city;
        this.state = state;
    }
    
    public String getId() { return id; }
    
    @Override
    public String toString() {
        return address + ", " + city + ", " + state;
    }
}