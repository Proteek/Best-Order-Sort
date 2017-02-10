/**
 *	Copyright [2017] [Proteek Chandan Roy]
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *    
 *  This file is a StandAlone software that implemented non-dominated sorting algorithm of the following paper
 *  Please cite this paper whenever the code is used.
 *  Bibtex entry
 *  @inproceedings{Roy:2016:BOS:2908961.2931684,
	 author = {Roy, Proteek Chandan and Islam, Md. Monirul and Deb, Kalyanmoy},
	 title = {Best Order Sort: A New Algorithm to Non-dominated Sorting for Evolutionary Multi-objective Optimization},
	 booktitle = {Proceedings of the 2016 on Genetic and Evolutionary Computation Conference Companion},
	 series = {GECCO '16 Companion},
	 year = {2016},
	 isbn = {978-1-4503-4323-7},
	 location = {Denver, Colorado, USA},
	 pages = {1113--1120},
	 numpages = {8},
	 url = {http://doi.acm.org/10.1145/2908961.2931684},
	 doi = {10.1145/2908961.2931684},
	 acmid = {2931684},
	 publisher = {ACM},
	 address = {New York, NY, USA},
	 keywords = {best order sort, layers of maxima, maximal vector computation, multi-objective optimization, non-dominated sorting, pareto set, pareto-efficiency, skyline operator, vector sorting},
} 
 * @author Proteek Chandan Roy, Department of CSE, Michigan State University, USA
 * Contact: royprote@egr.msu.edu, proteek_buet@yahoo.com
 */


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class BestOrderSortStandAlone {

	int m1=-1;
	double population[][];
	int [][]allrank;
	MergeSort mergesort;
	int rank[];
	boolean []set;
	LinkedList [][] list;
	LinkedList []L;//two dimension
	double b[];//two dimension
	int totalfront = 0;
	LinkedList[] Front;
	int s;
	int n;
	int m, m2;
	int obj,obj_rank;
	int []size;
	int []lex_order;
	
	long start, end, endTime2;
	double time, comparison, time_sort, time_domination, comparison_sort, comparison_domination;
	
	
	public void best_order_sort(double [][] population, boolean debug, boolean printinfo) 
	{
		
		initialize(population, debug, printinfo);
		
		if(m==2)
		{
			extended_kung_sort_two_dimension();		
		}
		else
		{
			best_sort(debug,printinfo,n,m1);
		}
		end = System.nanoTime();
		mini_stat();
		
		if(printinfo)
		{			
			System.out.println("Sorting time  = "+time_sort+" ms");
			System.out.println("Domination check time = "+time_domination+" ms");
			System.out.println("Total Running time = "+time+" ms");
			printInformation(n, m, debug, rank, totalfront, comparison);
		}
	}
	public void best_sort(boolean debug, boolean printinfo,int n, int m)
	{
		int i, j, total=0, i2;
		boolean dominated;
		Node head;

		rank = new int[n];
		boolean []set = new boolean[n];
		list = new LinkedList[m][n];
		allrank = new int[n][m];
		lex_order = new int[n];

		for(j=0;j<m;j++)
		{
			for(i=0;i<n;i++)
			{
				list[j][i] = new LinkedList();
				allrank[i][j]=i;
			}
		}

		mergesort.setrank(allrank);
		start = System.nanoTime();
		sortingValues();
		endTime2 = System.nanoTime();
		
		for(i=0;i<n;i++)//n
		{
			for(obj=0;obj<m;obj++)
			{
				s = allrank[i][obj];
				if(set[s])//s is already ranked
				{
					list[obj][rank[s]].addStart(s);
					continue;
				}
				set[s]=true;
				total++;
				i2=0;
				while(true)
				{
					dominated=false;
					head = list[obj][i2].start;	
					while(head!=null)
					{
						if(dominates(head.data,s))
						{
							dominated = true;
							break;
						}
						head = head.link;
					}
					if(!dominated) //not dominated
					{
						list[obj][i2].addStart(s);
						rank[s]= i2;
						break;
					}
					else //dominated
					{
						if(i2<totalfront)
						{
							i2++;
						}
						else //if it is last node(with highest rank)
						{
							
							totalfront++;
							rank[s] = totalfront;
							list[obj][rank[s]].addStart(s);
							break;	
						}
					}
					
				}//while(true)
			}
			if(total==n)
			{
				break;
			}
		}
		totalfront++;
	}
	
	/**
	 * Non-domination check, Although previously sorted by lexicographic order
	 * it may be true that those solutions are identical
	 * @param p1
	 * @param p2
	 * @return
	 */
	public boolean dominates(int p1, int  p2)//one way domination check
	{
		boolean equal = true;
		int i;
		for(i=0;i<m;i++)
		{
			
			if(population[p1][i] > population[p2][i])
			{
				comparison++;
				return false;
			}
			else if(equal && population[p1][i] < population[p2][i])
			{
				comparison = comparison+2;
				equal = false;
			}
		}
		
		if(equal)//both solutions are equal
			return false;
		else //dominates
			return true;
	}
	/**
	 * Sorting population by each objective
	 */
	public void sortingValues() 
	{
		int j;
		mergesort.sort(0);//Sorting first objectives and get lexicographic order
		allrank=mergesort.getrank();
		for(j=1;j<n;j++)
		{
			lex_order[allrank[j][0]] = j;  
		}
		mergesort.setLexOrder(lex_order);
		for (j=1;j<m1;j++)
		{
			mergesort.sort_specific(j);
			comparison = comparison + mergesort.comparison;
		}
		allrank = mergesort.getrank();
	}
	
	/**
	 * Initialize all variables
	 * @param population
	 * @param debug
	 * @param printinfo
	 */
	private void initialize(double[][] population, boolean debug, boolean printinfo)
	{
		if(printinfo)
		{
			System.out.println("\nStarting Best Order Sort");
		}
		this.population = population;
		mergesort = new MergeSort();
		n = population.length;
		m = population[0].length;
		mergesort.setPopulation(population);
		comparison=0;
		m1 = m;//change this value to m1 = log(n) when m is very high
	}
	/**
	 * Calculation of time and other possible statistics
	 */
	private void mini_stat()
	{
		time = (end-start)*1.0/1000000.0;
		time_sort = (endTime2 - start)*1.0/1000000.0;
		time_domination = (end - endTime2)*1.0/1000000.0;
	}
		
	/**
	 * This algorithm runs O(nlogn) algorithm for two dimensional non-dominated sorting problem
	 */
	public void extended_kung_sort_two_dimension()
	{
		//Initialization
		int i,j, low, high, middle;
		double key;
		
		rank = new int[n];//ranks of solutions
		allrank = new int[n][m];//partial lexicographical ordering of x-axis values 
		b = new double[n];
		L = new LinkedList[n];
		for(j=0;j<m;j++)
		{
			for(i=0;i<n;i++)
			{
				allrank[i][j]=i;
			}
		}
		start = System.nanoTime();
		mergesort.setrank(allrank);
		start=System.nanoTime();	
		mergesort.sort(0);;//lexicographic sort
		endTime2 = System.nanoTime();
		comparison = comparison + mergesort.comparison;
		
		
		b[0] = population[allrank[0][0]][1];//y-value of first rank solution
		rank[allrank[0][0]] = 0; //rank of first solution is already found
		totalfront = 1;
		L[0] = new LinkedList();
		for(i=1;i<n;i++)
		{
			s = allrank[i][0];//take the solution id
			key = population[s][1];//the field we would consider
			
			//-------------Go over all points----------------------//
			low = 0;			
			high = totalfront - 1;
		         
			while(high >= low) 
			{
				middle = low + ((high - low) / 2); 
					
				if(key < b[middle]) //it has low rank, numerically
				{
					comparison++;
					high = middle - 1;
				}
				if(key >= b[middle]) //it has high rank, numerically
				{
					comparison = comparison+2;
					low = middle + 1;
				}
			}
				
			if(low==totalfront)
			{
				totalfront = totalfront+1;
			}
			rank[s] = low;
			b[low] = key;
		}
	}
	
	/**
	 * Class that takes care of lexicographic sorting of objectives 
	 * @author Proteek Roy
	 *
	 */
	class MergeSort 
	{
		//local variables
		int[] helper;
		double [][]population;
		int n;
		int left;
		int right;
		int largest;
		int obj;
		boolean check;
		int [][] rank;
		int comparison;
		int [] lex_order;
		
		
		public void setrank(int [][] rank)//set ranking information
		{
			this.rank=rank;
		}
		public int[][] getrank()//get ranking information
		{
			return rank;
		}
		
		public void setPopulation(double [][]pop)//set local population
		{
			this.population=pop;
			this.n=population.length;
			helper=new int[n];
			comparison = 0;
		}
		
		public void setLexOrder(int []order)
		{
			this.lex_order = order;
		}
		
		/**
		 * Sort objectives obj
		 * @param obj
		 */
		
		public void sort(int obj)//merge sort main
		{
			this.obj=obj;
			n = population.length;
			mergesort(0, n-1);

		}
		
		/**
		 * merge sort algorithm O(nlogn) sorting time
		 */
		private void mergesort(int low, int high) 
		{
			// check if low is smaller then high, if not then the array is sorted
			if (low < high) 
			{
				// Get the index of the element which is in the middle
				int middle = low + (high - low) / 2;
				// Sort the left side of the array
				mergesort(low, middle);
				// Sort the right side of the array
				mergesort(middle + 1, high);
				// Combine them both
				merge(low, middle, high);
			 }
		}
		
		private void merge(int low, int middle, int high) 
		{
			// Copy both parts into the helper array
			for (int i = low; i <= high; i++) 
			{
				helper[i] = rank[i][obj];
			}
			  
			int i = low;
			int j = middle + 1;
			int k = low;
			// Copy the smallest values from either the left or the right side back
			// to the original array
			while (i <= middle && j <= high) 
			{
				if (population[helper[i]][obj] < population[helper[j]][obj]) 
				{
					comparison++;
					rank[k][obj] = helper[i];
					i++;
				} 
				else if(population[helper[i]][obj] > population[helper[j]][obj]) 
				{
					comparison = comparison+2;
					rank[k][obj] = helper[j];
					j++;
				}
				else //two values are equal
				{
					comparison = comparison+2;
					check=lexicopgraphic_dominate(helper[i],helper[j]);
					if(check)
					{
						rank[k][obj] = helper[i];
						i++;
					}
					else
					{
						rank[k][obj] = helper[j];
						j++;
					}
				}
				k++;
			}
			while(i<=middle)
			  {
				  rank[k][obj]=helper[i];
				  k++;
				  i++;
			  }
			  while(j<=high)
			  {
				  rank[k][obj]=helper[j];
				  k++;
				  j++;
			  }

		}
		public void sort_specific(int obj)//uses lexicographical order of 1st dimension
		{
			this.obj=obj;
			n = population.length;
			mergesort_specific(0, n-1);
		}
		
		/**
		 * Merge sort with the help of lexicographic order got by sorting first objectives
		 * @param low
		 * @param high
		 */
		private void mergesort_specific(int low, int high) 
		{
			  // check if low is smaller then high, if not then the array is sorted
			  if (low < high) 
			  {
				  // Get the index of the element which is in the middle
				  int middle = low + (high - low) / 2;
				  // Sort the left side of the array
				  mergesort_specific(low, middle);
				  // Sort the right side of the array
				  mergesort_specific(middle + 1, high);
				  // Combine them both
				  merge_specific(low, middle, high);
			  }
			  
		}
		private void merge_specific(int low, int middle, int high) 
		{

			// Copy both parts into the helper array
			for (int i = low; i <= high; i++) 
			{
				helper[i] = rank[i][obj];
			}
			  
			int i = low;
			int j = middle + 1;
			int k = low;
			// Copy the smallest values from either the left or the right side back to the original array
			while (i <= middle && j <= high) 
			{
				if (population[helper[i]][obj] < population[helper[j]][obj]) 
				{
					comparison++;
					rank[k][obj] = helper[i];
					i++;
				} 
				else if(population[helper[i]][obj] > population[helper[j]][obj]) 
				{
					comparison = comparison+2;
					rank[k][obj] = helper[j];
					j++;
				}
				else //two values are equal
				{			  
					comparison = comparison+2;
					if(lex_order[helper[i]]<lex_order[helper[j]])
					{
						rank[k][obj] = helper[i];
						i++;
					}
					else
					{
						rank[k][obj] = helper[j];
						j++;
					}
				}
				k++;
			}
			while(i<=middle)
			{
				rank[k][obj]=helper[i];
				k++;
				i++;
			}
			while(j<=high)
			{
				rank[k][obj]=helper[j];
				k++;
				j++;
			}

		}

		/**
		 * check whether p1 lexicographically dominates p2
		 * @param p1
		 * @param p2
		 * @return
		 */
		public boolean lexicopgraphic_dominate(int p1, int p2)
		{
			int i;

			for(i=0;i<population[0].length;i++)
			{
				comparison++;
				if(population[p1][i] == population[p2][i])
					continue;
				else if(population[p1][i] < population[p2][i])
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			
			return true;
	 	}
	  	
	}

	/**
	 * Read population from a text file separated by whitespace e.g tab/space, one solution per line
	 * @param n, size of population
	 * @param m, objective of population
	 * @param filename, name of text file
	 * @param population, memory to hold population
	 */
	public void read_population(int n, int m, String filename,double [][] population) 
	{
		int i=0,j;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String strLine;
			while ((strLine = br.readLine()) != null)   
			{
				
				String[] tokens = strLine.split("\\s+");
				strLine=strLine.trim();
				if(i==n)
				{
					break;
				}
				for(j=0;j<m;j++)
				{
					try{
					population[i][j]=Double.parseDouble(tokens[j]);
					}
					catch (Exception e)
					{
						System.err.println("Error: " + e.getMessage());
					}
				}
				i++;
			}
			br.close();
		}
		catch (IOException e)
		{
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Printing all solutions from each front
	 * @param n
	 * @param m
	 * @param debug
	 * @param rank
	 * @param totalfront
	 * @param comparison_dominated
	 */
	public void printInformation(int n, int m, boolean debug, int []rank, int totalfront, double comparison)
	{
		int i,k;
		Node head;
		LinkedList[] F=new LinkedList[totalfront];
		for(i=0;i<totalfront;i++)
		{
			F[i]=new LinkedList();
		}
		for(i=0;i<n;i++)
		{
			F[rank[i]].addStart(i);
		}
		
		
		for(i=0;i<totalfront;i++)
		{
			if(F[i]==null)
				break;
			if(F[i].start==null)
				break;
			head = F[i].start;
			k=0;
			while(head!=null)
			{
				k++;
				head=head.link;
			}
			System.out.println("Number of elements in front ["+(i+1)+"] is "+k);
			if(debug)
			{
				if(F[i].start!=null)
				{
					System.out.print(" --> ");
					head = F[i].start;	
					while(head!=null)
					{
						System.out.print((head.data)+", ");
						head=head.link;
					}
					System.out.println();
				}
			}
		}
		System.out.println("Number of fronts = "+totalfront);
		System.out.println("Number of Comparisons for  domination = "+comparison);
		System.out.println("Total Number of elements is "+n);
		System.out.println("Total Number of objectives is "+ m);
	}
	
	/**
	 * Class to save solutions in a list
	 * @author Proteek Roy
	 *
	 */
	private class LinkedList
	{
	    protected Node start;
	    
	    public LinkedList()// Constructor
	    {
	        start = null;
	    }
	    
	    //Function to insert an element at the beginning 
	    public void addStart(int val)
	    {
	        Node nptr = new Node(val, start);    
	        start = nptr;
	    }
	}

}