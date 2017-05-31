package de.thelearningtriangle.core.overworld;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.jogamp.nativewindow.util.Point;

import de.thelearningtriangle.core.overworld.field.AbstractField;
import de.thelearningtriangle.core.overworld.field.DeathField;
import de.thelearningtriangle.core.overworld.field.FieldType;
import de.thelearningtriangle.core.overworld.field.WallField;
import de.thelearningtriangle.core.triangle.LearningTriangle;

public class TriangleOverworld {

	private Optional<AbstractField[][]> map;

	private List<TrianglePosition> trianglePositions = new ArrayList<TrianglePosition>();

	private Random random;

	public TriangleOverworld(Random random) {
		this.random = random;
	}

	public void setMap(AbstractField[][] field) {
		this.map = Optional.of(field);
	}

	private AbstractField[][] getMap() throws NoMapException {
		return map.orElseThrow(NoMapException::new);
	}

	public int getSize() throws NoMapException {
		return getMap().length;
	}

	public AbstractField getField(int x, int y) throws NoMapException {
		return getMap()[x][y];
	}

	public void setTriangle(int x, int y) throws NoMapException, FieldAccessException {
		setTriangle(new Point(x, y));
	}

	public void setTriangle(Point point) throws NoMapException, FieldAccessException {
		LearningTriangle learningTriangle = new LearningTriangle();
		getMap()[point.getX()][point.getY()].access(learningTriangle);
		trianglePositions.add(new TrianglePosition(point, learningTriangle));
	}

	public Point getRandomSpawningPoint() throws NoMapException {
		List<Point> possibleSpawningPoints = new ArrayList<Point>();
		AbstractField[][] currentMap = getMap();
		for (int y = 0; y < currentMap.length; y++) {
			AbstractField[] abstractFields = currentMap[y];
			for (int x = 0; x < abstractFields.length; x++) {
				AbstractField abstractField = abstractFields[x];
				if (!((WallField.class.isInstance(abstractField)) | DeathField.class.isInstance(abstractField))) {
					possibleSpawningPoints.add(new Point(x, y));
				}
			}
		}
		return possibleSpawningPoints.get(random.nextInt(possibleSpawningPoints.size()));
	}

	public void moveTriangle(TrianglePosition trianglePosition, Direction direction) throws NoMapException {
		Point point = trianglePosition.getPoint();
		int x = point.getX();
		int y = point.getY();

		int newX = x + direction.getChangeInX();
		int newY = y + direction.getChangeInY();

		try {
			LearningTriangle learningTriangle = trianglePosition.getLearningTriangle();
			getMap()[newX][newY].access(learningTriangle);

			trianglePositions.remove(trianglePosition);
			trianglePositions.add(new TrianglePosition(new Point(newX, newY), learningTriangle));

		} catch (FieldAccessException e) {
			System.out.println(MessageFormat.format("Triangle at pos [{0},{1}] can''t move in direction: {2}", x, y,
					direction.name()));
		}
	}

	public List<Integer> getVisionVectorFor(Point point) {
		List<Integer> fieldVector = new ArrayList<Integer>();

		int visionField = 3;
		for (int visionX = -visionField; visionX < visionField; visionX++) {
			for (int visionY = -visionField; visionY < visionField; visionY++) {
				AbstractField field = null;

				try {
					field = getField(point.getX() + visionX, point.getY() + visionY);
				} catch (Exception e) {
					field = FieldType.WALL.newInstance();
				} finally {
					fieldVector.add(FieldType.getIdFor(field));
				}
			}
		}
		return fieldVector;
	}

	public List<TrianglePosition> getTrianglePositions() {
		return trianglePositions;
	}
}