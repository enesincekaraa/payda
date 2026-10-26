package com.enesincekara.payda.sharing.donation.domain;

public class Donation {
    private final String title;
    private final int totalPackageCount;
    public Donation(String title, int totalPackageCount) {
        if (title==null||title.isBlank()){
            throw new IllegalArgumentException(
                    "Donation title must not be blank"
            );
        }
        if (totalPackageCount<=0){
            throw new IllegalArgumentException(
                    "Total package count must be greater than zero"
            );
        }
        this.title = title;
        this.totalPackageCount = totalPackageCount;
    }

    public String getTitle() {
        return title;
    }
    public int getTotalPackageCount() {
        return totalPackageCount;
    }


}
