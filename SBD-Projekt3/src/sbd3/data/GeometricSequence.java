package sbd3.data;

public class GeometricSequence implements Comparable<GeometricSequence> {
	
	/**
	 * Index i pozycja w pliku indeksów
	 */
	int id;
	double firstTerm;
	double multiplier;
	
	public GeometricSequence() {
		this.id = 0;
		this.firstTerm = 0;
		this.multiplier = 0;
	}
	
	public GeometricSequence(Double firstTerm, Double multiplier) {
		this.firstTerm = firstTerm;
		this.multiplier = multiplier;
	}
	
	public GeometricSequence(Integer id, Double firstTerm, Double multiplier) {
		this(firstTerm, multiplier);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getFirstTerm() {
		return firstTerm;
	}

	public void setFirstTerm(double firstTerm) {
		this.firstTerm = firstTerm;
	}

	public double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	@Override
	public int compareTo(GeometricSequence arg0) {
		return (getId() > arg0.getId() ? 1 : (getId() < arg0.getId() ? -1 : 0));
	}
}
