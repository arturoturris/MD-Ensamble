package controller;

import Extra.ARFFFile;
import Extra.MyTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import model.MyFile;
import view.FormKnn;
import view.FormNaiveBayes;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.Instances;

public class ControllerFormNaiveBayes implements ActionListener{
    ArrayList<MyFile> filesAvailable;
    FormNaiveBayes view;
    Instances data;
    Classifier classifier;
    Classifier buffer;
    
    public ControllerFormNaiveBayes(FormNaiveBayes view,ArrayList<MyFile> filesAvailable){
        this.view = view;
        this.filesAvailable = filesAvailable;
        this.data = null;
        this.classifier = null;
        this.buffer = null;
        
        view.btnComprobar.addActionListener(this);
        view.btnEntrenarClasificador.addActionListener(this);
        view.btnModificarClasificador.addActionListener(this);
    }
    
    private void resetFields(){
        view.tblMeanDeviation.setText(null);
        view.cmbArchivoUitilizar.removeAllItems();
        view.numberFolds.setValue(2);
        view.jEditorPane1.setText(null);
        MyTable.clearTable(view.tblMatrizDeConfusion);
        view.lblCorrectlyClassified.setText(null);
        view.lblBadlyClassified.setText(null);
    }
    
    public void loadListOfFiles(){
        view.cmbArchivoUitilizar.removeAllItems();
        
        for(MyFile file: filesAvailable){
            view.cmbArchivoUitilizar.addItem(file.getFileName());
        }
    }
    
    private void setData(){
        try {
            String filePath = MyFile.getFilePath(filesAvailable, view.cmbArchivoUitilizar.getItemAt(view.cmbArchivoUitilizar.getSelectedIndex()));
            data = ARFFFile.readArffFile(filePath);
            data.setClassIndex(data.numAttributes()-1);
        } catch (Exception ex) {
            Logger.getLogger(ControllerFormKnn.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void establishClassifier(){
        classifier = buffer;
    }
    
    public void evaluateModel(){
        try {
            //READING FILE SELECTED
            setData();
            
            //EVALUATING
            buffer = new NaiveBayes();
            int folds = view.numberFolds.getValue().toString().equals("PORCENTAGE")? 100/(int)view.validacionPrc.getValue() : (int)view.numberFolds.getValue();
            Evaluation evaluation = new Evaluation(data);
            evaluation.crossValidateModel(buffer,data,folds,new Random(Long.parseLong(view.jEditorPane1.getText())));
            
            //IF CLASS IS NOMINAL DISPLAY CONFUSION MATRIX
            if(data.attribute(data.classIndex()).isNominal()){
                double [][]confusionMatrix = evaluation.confusionMatrix();

                //PRINTING RESULTS
                String []headers = new String[data.numClasses()];
                Attribute classAttribute = data.attribute(data.numAttributes()-1);
                Enumeration classValues = classAttribute.enumerateValues();
                //GETTING HEADERS
                for (int i=0; classValues.hasMoreElements();i++){
                    headers[i] = (String)classValues.nextElement();
                }
                
                //PRINTING GENERAL INFORMATION MEAN AND DEVIATION
                buffer.buildClassifier(data);
                view.tblMeanDeviation.setText(buffer.toString());
                view.lblCorrectlyClassified.setText(String.format("%.2f",evaluation.pctCorrect()));
                view.lblBadlyClassified.setText(String.format("%.2f", evaluation.pctIncorrect()));
                MyTable.displayConfusionMatrix(view.tblMatrizDeConfusion, confusionMatrix, headers);
            }
            
            
            //IF CLASS IS NUMERIC DISPLAY 
            else{
                final JPanel panel = new JPanel();
                String message = "La clase no es nominal. No es posible mostrar la matriz de confusión.";
                JOptionPane.showMessageDialog(panel, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            
            
            
        } catch (Exception ex) {
            Logger.getLogger(ControllerFormNaiveBayes.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public Classifier getClassifier(){
        return classifier;}
    
    public FormNaiveBayes getView(){
        return view;}
    
    @Override
    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();
        
        if(source == view.btnComprobar){
            evaluateModel();
            view.btnEntrenarClasificador.setEnabled(true);
        }
        
        else if(source == view.btnModificarClasificador){
        
        }
        
    }
}
