package com.example.practicapmdm.controllers;

import com.example.practicapmdm.models.Favoritos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class fileControllers {

    public ArrayList fileReader(){
        ArrayList<Favoritos> arrayFavoritos= new ArrayList();
        try {
            BufferedReader br = new BufferedReader(new FileReader("favorites.txt"));
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] tokens = linea.split(":");
                fileReader().add(tokens[0]);
            }
            br.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
return arrayFavoritos;
    }
    public void fileWriter(ArrayList <Favoritos> arrayFavoritos){
        File file = new File("favorites.txt");
        try {
            FileWriter fw = new FileWriter(file);
            for (int i = 0; i < arrayFavoritos.size(); i++) {
                fw.write(arrayFavoritos.get(i).getFavoritos());
                fw.append(":");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
