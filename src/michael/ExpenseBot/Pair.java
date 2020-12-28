package michael.ExpenseBot;

public class Pair {
	String x,y;

    public Pair(String x,String y){
        this.x = x;
        this.y = y;
    }

    
    public boolean equals(Object o){
        if(o instanceof Pair){
            Pair p = (Pair) o;
            return (p.x.equals(x) && p.y.equals(y));
        } else{
            return false;
        }
    }
    
    public String getX() {
    	return this.x;
    }
    
    public String getY() {
    	return this.y;
    }

}
