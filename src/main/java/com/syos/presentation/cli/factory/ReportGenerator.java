package com.syos.presentation.cli.factory;

import com.syos.domain.service.Report;

public abstract class ReportGenerator {

    public abstract Report createReport(); //implementation in the generatos

    public void setTransactionType(String type) {}

    public void run() {
        //this.createReport and below one same
        Report report = createReport(); //get the report object(factory) (returns in every imple)
        if (report != null) {
            report.generate();//generate the returned object report , template exec
        }
    }
}
//since createReport factoyr method retruns a specific report..
//can use generate method since abstract report has the generate method, and the specific report is a child of abstract reoprt
