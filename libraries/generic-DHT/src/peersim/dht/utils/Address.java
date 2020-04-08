package peersim.dht.utils;

public class Address {
    private final double location;

    public Address(double address){
        this.location = Math.abs(address) % 1.0;
    }

    public double distance(Address b){
        if (this.location > b.location)
            return Math.min(this.location - b.location, 1.0 - this.location + b.location);
        return Math.min(b.location - this.location, 1.0 - b.location + this.location);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Address))
            return false;

        Address rhs = (Address) obj;
        return rhs.location == this.location;
    }

    @Override
    public String toString() {
        return String.format("%f", this.location);
    }
}
