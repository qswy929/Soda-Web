package com.app.predict;

import java.math.BigDecimal;

/**
 * Copyright 2011 Nishant Chandra
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Given a time series, say a complete monthly data for 12 months, the Holt-Winters smoothing and forecasting 
 * technique is built on the following formulae (multiplicative version):
 * 然而我把原作者的季节乘法模型改成了无季节模型
 * 
 * St[i] = alpha * y[i] / It[i - period] + (1.0 - alpha) * (St[i - 1] + Bt[i - 1])
 * Bt[i] = gamma * (St[i] - St[i - 1]) + (1 - gamma) * Bt[i - 1]
 * Ft[i + m] = (St[i] + (m * Bt[i])) * It[i - period + m]
 * 
 * Note: Many authors suggest calculating initial values of St, Bt and It in a variety of ways, but
 * some of them are incorrect e.g. determination of It parameter using regression. I have used
 * the NIST recommended methods.
 *   
 * For more details, see:
 * http://adorio-research.org/wordpress/?p=1230
 * http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm
 * 
 * @author Nishant Chandra
 *
 */
public class HoltWinters {

	/**
	 * This method is the entry point. It calculates the initial values and returns the forecast
	 * for the m periods.
	 * 
	 * @param y - Time series data.
	 * @param alpha - Exponential smoothing coefficients for level components.
	 * @param beta - Exponential smoothing coefficients for trend components.
	 * @param perdiod - A complete season's data consists of L periods. And we need to estimate 
	 * the trend factor from one period to the next. To accomplish this, it is advisable to use 
	 * two complete seasons; that is, 2L periods.   季节周期，这里为1
	 * @param m - Extrapolated future data points.  预测的期数，这里为1
	 * @param debug - Print debug values. Useful for testing.
	 * 
     *				4 quarterly
     *     			7 weekly.
     *     			12 monthly
	 */
	public BigDecimal forecast(double[] y, double alpha, double beta, int period, int m, boolean debug) {

		if (y == null) {
			return null;
		}

		int seasons = y.length / period;
		double a0 = calculateInitialLevel(y, period);
		double b0 = calculateInitialTrend(y, period);
		//double[] initialSeasonalIndices = calculateSeasonalIndices(y, period, seasons);

		if (debug) {
			System.out.println(String.format(
					"Total observations: %d, Periods %d", y.length,
					seasons, period));
			System.out.println("Initial level value a0: " + a0);
			System.out.println("Initial trend value b0: " + b0);
			//printArray("Seasonal Indices: ", initialSeasonalIndices);
		}

		double[] forecast = calculateHoltWinters(y, a0, b0, alpha, beta, period, m, debug);

		if (debug) {
			printArray("Forecast", forecast);
		}
		BigDecimal b = new BigDecimal(forecast[forecast.length-1]);
		b = b.setScale(2, BigDecimal.ROUND_HALF_UP);
		return b;
	}
	
	/**
	 * This method realizes the Holt-Winters equations.
	 * 
	 * @param y
	 * @param a0
	 * @param b0
	 * @param alpha
	 * @param beta
	 * @param initialSeasonalIndices
	 * @param period
	 * @param m
	 * @param debug
	 * @return - Forecast for m periods.
	 */
	private double[] calculateHoltWinters(double[] y, double a0, double b0, double alpha,
			double beta, int period, int m, boolean debug) {
		
		double[] St = new double[y.length];
		double[] Bt = new double[y.length];
		//double[] It = new double[y.length];
		double[] Ft = new double[y.length + m];
		
		//Initialize base values
		St[1] = a0;
		Bt[1] = b0;
		   
//		for (int i = 0; i < period; i++) {
//			It[i] = initialSeasonalIndices[i];
//		}
		
		//Ft[m] = (St[0] + (m * Bt[0])) * It[0];//This is actually 0 since Bt[0] = 0
		//Ft[m + 1] = (St[1] + (m * Bt[1])) * It[1];//Forecast starts from period + 2
		Ft[m] = (St[0] + (m * Bt[0]));//This is actually 0 since Bt[0] = 0
		Ft[m + 1] = (St[1] + (m * Bt[1]));//Forecast starts from period + 2
		
		//Start calculations
		for (int i = 2; i < y.length; i++) {

			//Calculate level smoothing
			
			St[i] = alpha * y[i] + (1.0 - alpha) * (St[i - 1] + Bt[i - 1]);
		
			//Calculate trend smoothing
	        Bt[i] = beta * (St[i] - St[i - 1]) + (1 - beta) * Bt[i - 1];
	        
//	        //Calculate seasonal smoothing
//	        if((i - period) >= 0) {
//	        	It[i] = gamma * y[i] / St[i] + (1.0 - gamma) * It[i - period];
//	        }
	                                                      
            //Calculate forecast
	        if( ((i + m) >= period) ){
	        	//Ft[i + m] = (St[i] + (m * Bt[i])) * It[i - period + m];
	        	Ft[i + m] = St[i] + (m * Bt[i]);
	        }
	        
	        if(debug){
				System.out.println(String.format(
						"i = %d, y = %f, S = %f, Bt = %f, F = %f", i,
						y[i], St[i], Bt[i], Ft[i]));
	        }
		}
		
		return Ft;
	}

	/**
	 * See: http://robjhyndman.com/researchtips/hw-initialization/
	 * 1st period's average can be taken. But y[0] works better.
	 * 
	 * @return - Initial Level value i.e. St[1]
	 */
	private double calculateInitialLevel(double[] y, int period) {

		/**		
 		double sum = 0;

		for (int i = 0; i < period; i++) {
			sum += y[i];
		}
		
		return sum / period;
		 **/
		return y[0];
	}
	
	/**
	 * See: http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm
	 * 
	 * @return - Initial trend - Bt[1]
	 */
	private double calculateInitialTrend(double[] y, int period){
		
		double sum = 0;
		
		for (int i = 0; i < period; i++) {			
			sum += (y[period + i] - y[i]);
		}
		
		return sum / (period * period);
	}
	
	/**
	 * See: http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm
	 * 
	 * @return - Seasonal Indices.
	 */
//	private double[] calculateSeasonalIndices(double[] y, int period, int seasons){
//						
//		double[] seasonalAverage = new double[seasons];
//		double[] seasonalIndices = new double[period];
//		
//		double[] averagedObservations = new double[y.length];
//		
//		for (int i = 0; i < seasons; i++) {
//			for (int j = 0; j < period; j++) {
//				seasonalAverage[i] += y[(i * period) + j];
//			}
//			seasonalAverage[i] /= period;
//		}
//		
//		for (int i = 0; i < seasons; i++) {
//			for (int j = 0; j < period; j++) {
//				averagedObservations[(i * period) + j] = y[(i * period) + j] / seasonalAverage[i];				
//			}			
//		}
//		
//		for (int i = 0; i < period; i++) {
//			for (int j = 0; j < seasons; j++) {
//				seasonalIndices[i] += averagedObservations[(j * period) + i];
//			}			
//			seasonalIndices[i] /= seasons;
//		}
//		
//		return seasonalIndices;
//	}
	
	/**
	 * Utility method to pring array values.
	 * 
	 * @param description
	 * @param data
	 */
	private void printArray(String description, double[] data){
		
		System.out.println(String.format("******************* %s *********************", description));
		
		for (int i = 0; i < data.length; i++) {
			System.out.println(data[i]);
		}
		
		System.out.println(String.format("*****************************************************************", description));
	}
}
