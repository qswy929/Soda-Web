package com.app.executor;

public class QueryExec {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
        QueryTimer qt = new QueryTimer();
        qt.queryNow();      
        qt.queryEveryHour();
        qt.queryEvery5Mintue();
        qt.queryEveryHalfHour();
	}

}
