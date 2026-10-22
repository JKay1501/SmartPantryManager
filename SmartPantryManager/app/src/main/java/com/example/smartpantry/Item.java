package com.example.smartpantry;

/** A single pantry ingredient. Quantities are always stored in base units (g, ml, pcs). */
public class Item {
    public long id;
    public String name;
    public double qty;
    public String unit;
    public String expiry; // yyyy-MM-dd or empty

    public Item(long id, String name, double qty, String unit, String expiry) {
        this.id = id; this.name = name; this.qty = qty; this.unit = unit; this.expiry = expiry;
    }

    public String qtyText() {
        return (qty == Math.floor(qty)) ? String.valueOf((long) qty) : String.valueOf(qty);
    }
}
