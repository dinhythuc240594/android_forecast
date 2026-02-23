package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<String> foodLiveData = new MutableLiveData<>();

    public void setFood(String food) {
        foodLiveData.setValue(food);
    }

    public LiveData<String> getFood() {
        return  foodLiveData;
    }

}
