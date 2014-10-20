package com.buddyware.treefrog.local.model;

import java.util.Observable;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ListPropertyTest {
	
    public ListPropertyTest() {
    // Create an observable list     
    ListProperty<String> lp =
              new SimpleListProperty<String>(FXCollections.observableArrayList());
              
    // Add invalidation, change, and List change listeners
      //   lp.addListener(ListPropertyTest::invalidated);
    lp.addListener(ListPropertyTest::changed);
         //lp.addListener(ListPropertyTest::onChanged);                                                  
              
     System.out.println ("add()");          
    lp.add("one") ;
    lp.add("two");
    
    System.out.println ("\nset()");
         // Change the list itself
         lp.set(FXCollections.observableArrayList("two", "three", "four", "five"));
    
    System.out.println ("\nremove()");
    	lp.remove("three");
    }
    
    public static void invalidated(Observable list) {
         System.out.println("List property is invalid.");
    }
    
    public static void changed(ObservableValue<? extends ObservableList<String>> observable,
                                  ObservableList<String> oldList,
    ObservableList<String> newList ) {
         System.out.print("List Property has changed for " + observable);
         System.out.print(" Old List: " + oldList);
         System.out.println(", New List: " + newList);
    }
    
    public static void onChanged(ListChangeListener.Change<? extends String> change) {
         while (change.next()) {
              String action =
                   change.wasPermutated() ? "Permutated":
                   change.wasUpdated() ? "Updated":
                   change.wasRemoved() && change.wasAdded() ? "Replaced":
                   change.wasRemoved() ? "Removed": "Added";
              
              System.out.print("Action taken on the list: " + action);     
         System.out.print(", Removedt: " + change.getRemoved());
         System.out.println(", Added: " + change.getAddedSubList());
         }
    }
}