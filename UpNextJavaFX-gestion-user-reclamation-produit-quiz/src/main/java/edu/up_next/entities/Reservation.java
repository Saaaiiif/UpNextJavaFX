package edu.up_next.entities;

public class Reservation {
    private int id;
    private int event_id;
    private int user_id;
    private int quantity;
    private float total_price;

    public Reservation() {
    }

    public Reservation(int id, int event_id, int user_id, float total_price, int quantity) {
        this.id = id;
        this.event_id = event_id;
        this.user_id = user_id;
        this.total_price = total_price;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getTotal_price() {
        return total_price;
    }

    public void setTotal_price(float total_price) {
        this.total_price = total_price;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", event_id=" + event_id +
                ", user_id=" + user_id +
                ", quantite=" + quantity +
                ", total_price=" + total_price +
                '}';
    }
}
