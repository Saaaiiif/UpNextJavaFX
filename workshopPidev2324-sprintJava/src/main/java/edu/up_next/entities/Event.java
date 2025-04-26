package edu.up_next.entities;

import java.time.LocalDateTime;

public class Event {
    private int id;
    private int host_id;
    private String title;
    private String descrip;
    private String location;
    private LocalDateTime startdate;
    private LocalDateTime  enddate;
    private String  status_event;
    private String image;
    private int  ticket_count;
    private int  guest_id;
    private double  ticket_price;

    public Event() {
    }

    public Event(int id, int host_id, String title, String descrip, LocalDateTime startdate, LocalDateTime enddate, String status_event, int ticket_count, int guest_id, String location,String image , double ticket_price) {
        this.id = id;
        this.host_id = host_id;
        this.title = title;
        this.descrip = descrip;
        this.location = location;
        this.startdate = startdate;
        this.enddate = enddate;
        this.status_event = status_event;
        this.ticket_count = ticket_count;
        this.image = image;
        this.guest_id = guest_id;
        this.ticket_price = ticket_price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHost_id() {
        return host_id;
    }

    public void setHost_id(int host_id) {
        this.host_id = host_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescrip() {
        return descrip;
    }

    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }

    public LocalDateTime getStartdate() {
        return startdate;
    }

    public void setStartdate(LocalDateTime startdate) {
        this.startdate = startdate;
    }

    public LocalDateTime getEnddate() {
        return enddate;
    }

    public void setEnddate(LocalDateTime enddate) {
        this.enddate = enddate;
    }

    public String getStatus_event() {
        return status_event;
    }

    public void setStatus_event(String status_event) {
        this.status_event = status_event;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getTicket_count() {
        return ticket_count;
    }

    public void setTicket_count(int ticket_count) {
        this.ticket_count = ticket_count;
    }

    public int getGuest_id() {
        return guest_id;
    }

    public void setGuest_id(int guest_id) {
        this.guest_id = guest_id;
    }

    public double getTicket_price() {
        return ticket_price;
    }

    public void setTicket_price(double ticket_price) {
        this.ticket_price = ticket_price;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", host_id=" + host_id +
                ", title='" + title + '\'' +
                ", descrip='" + descrip + '\'' +
                ", location='" + location + '\'' +
                ", startdate=" + startdate +
                ", enddate=" + enddate +
                ", status_event='" + status_event + '\'' +
                ", image='" + image + '\'' +
                ", ticket_count=" + ticket_count +
                ", guest_id=" + guest_id +
                ", ticket_price=" + ticket_price +
                '}';
    }
}

