package com.example.erika.cookbook;

/**
 * Created by Erika on 1. 11. 2016.
 */
public class EANsurovinaO {
    public long eanNumber;
    public String foodstuff;

    public EANsurovinaO(){}

    public EANsurovinaO (long eanNumber, String foodstuff){
        this.setEanNumber(eanNumber);
        this.setFoodstuff(foodstuff);
    }

    public long getEanNumber() {
        return eanNumber;
    }

    public void setEanNumber(long eanNumber) {
        this.eanNumber = eanNumber;
    }

    public String getFoodstuff() {
        return foodstuff;
    }

    public void setFoodstuff(String foodstuff) {
        this.foodstuff = foodstuff;
    }
}
