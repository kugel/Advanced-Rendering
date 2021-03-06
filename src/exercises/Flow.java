package exercises;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import hilfsklassen.AdvancedTemplate;

/**
 * Vis. assignment 1
 * 19.03.2013
 * 
 * @author Andreas Gehle, 6603927
 * 
 * Visualization of a flow data set
 * 
 * - Arrows showing the direction of each water particle flowing
 * - Colors stands for heays/low flow as follow:
 * - - Getting closer to color red means the flow is heavy
 * - - Getting away to color red means the flow is low
 */
public class Flow extends AdvancedTemplate {

	private float[][] flow_data;
	private int[] flow_spatial_dim = new int[3];
	private int[] flow_ext_dim = new int[3];

	private float flow_min = -1;
	private float flow_max = 0;

	/**
	 * The main method
	 */
	public static void main(String[] args) {
		Flow template = new Flow();
		template.setVisible(true);
	}

	// ------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Flow() {
		super();
		setTitle("Visualization of a 2D flow slice");

		setModiFlag(MODI_DEPTH_BUFFERING);
		// setLightFlag(LIGHT_DIRECTIONAL);
		// setLightFlag(LIGHT_SHADING);

		// Parse data
		parseData("data/field2.irreg");

		System.out.println("Min is " + flow_min);
		System.out.println("Max is " + flow_max);
		System.out.println("Range is " + (flow_max-flow_min));
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);

		GL gl = drawable.getGL();
		gl.glDisable(GL.GL_LIGHTING);
	}

	private void parseData(String path) {
		System.out.println("Parsing data...");

		Scanner scan;
		File file = new File(path);

		try {
			scan = new Scanner(file);

			// First dim
			flow_spatial_dim[0] = scan.nextInt();
			// Second dim
			flow_spatial_dim[1] = scan.nextInt();
			// Extension of each dim
			flow_ext_dim[0] = scan.nextInt();
			flow_ext_dim[1] = scan.nextInt();
			flow_ext_dim[2] = scan.nextInt();
			// Third dim
			flow_spatial_dim[2] = scan.nextInt();

			// Data array
			flow_data = new float[6724][7];
			for (int line = 0, value = 0; scan.hasNext(); value++) {
				if (value == 6) {
					// Get length
					flow_data[line][6] = getLength(flow_data[line]);

					// Set max
					flow_max = Math.max(flow_max, flow_data[line][6]);

					// Set min
					if (flow_min < 0 || flow_min > flow_data[line][6]) {
						flow_min = flow_data[line][6];
					}

					// Norm u,v,w
					for (int i = 3; i < 6; i++) {
						flow_data[line][i] /= flow_data[line][6];
					}

					// Reset/Increase counters
					line++;
					value = 0;
				}
				flow_data[line][value] = Float.parseFloat(scan.next());
				// System.out.println(line+" - "+value);
			}

		} catch (FileNotFoundException fe) {
			System.out.println("FileNotFoundException : " + fe);
		}
	}

	// ------------------------------------------------------------

	public void displayDraw(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();

		gl.glEnable(GL.GL_COLOR_MATERIAL);

		// drawCoord(gl, 20);

		gl.glPushMatrix();
		{
			// center image on 2d base
			gl.glTranslatef(-11f, -11f, 0);

			// Maybe use dim for something...border etc. ?
			gl.glScalef(20, 20, 1);
			for (int i = 0; i < flow_data.length; i++) {
				gl.glColor3fv(dataToColor(flow_data[i][6]), 0);
				drawFlowArrow3D(gl, flow_data[i]);
			}
		}
		gl.glPopMatrix();

	}

	private float getLength(float[] coords) {
		return (float) Math.sqrt(Math.pow((coords[3]), 2)
				+ Math.pow((coords[4]), 2) + Math.pow((coords[5]), 2));
	}

	private void drawFlow(GL gl, float[] coords) {
		float stroke_length = 0.02f;

		gl.glPushMatrix();
		{
			gl.glBegin(GL.GL_LINES);
			{
				gl.glVertex3f(coords[0], coords[1], coords[2]);
				// relative movement
				gl.glVertex3f(coords[0] + stroke_length * coords[3], coords[1]
						+ stroke_length * coords[4], coords[2] + stroke_length
						* coords[5]);
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
	}

	private void drawFlowArrow(GL gl, float[] coords) {
		float stroke_length = 0.02f;
		float arrow_length = 0.002f;
		
		float[] normal = new float[]{-coords[4],coords[3],0};

		gl.glPushMatrix();
		{
			gl.glBegin(GL.GL_TRIANGLE_FAN);
			{
				gl.glVertex3f(coords[0] + arrow_length * normal[0], coords[1]
						+ arrow_length * normal[1], coords[2] + arrow_length
						* normal[2]);
				gl.glVertex3f(coords[0] - arrow_length * normal[0], coords[1]
						- arrow_length * normal[1], coords[2] - arrow_length
						* normal[2]);
				// relative movement
				gl.glVertex3f(coords[0] + stroke_length * coords[3], coords[1]
						+ stroke_length * coords[4], coords[2] + stroke_length
						* coords[5]);
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
	}

	private void drawFlowArrow3D(GL gl, float[] coords) {
		float stroke_length = 0.02f;
		float arrow_length = 0.002f;
		float arrow_depth = 1f;

		float[] normal = new float[] { -coords[4], coords[3], 0 };

		gl.glPushMatrix();
		{
			gl.glBegin(GL.GL_TRIANGLES);
			{
				//Back1
				gl.glVertex3f(coords[0] + arrow_length * normal[0], coords[1]
						+ arrow_length * normal[1], coords[2] + arrow_length
						* normal[2]);
				gl.glVertex3f(coords[0], coords[1], arrow_depth*coords[6]);
				gl.glVertex3f(coords[0] - arrow_length * normal[0], coords[1]
						- arrow_length * normal[1], coords[2] - arrow_length
						* normal[2]);
				

				//Back2
				gl.glVertex3f(coords[0] + arrow_length * normal[0], coords[1]
						+ arrow_length * normal[1], coords[2] + arrow_length
						* normal[2]);
				gl.glVertex3f(coords[0], coords[1], -arrow_depth*coords[6]);
				gl.glVertex3f(coords[0] - arrow_length * normal[0], coords[1]
						- arrow_length * normal[1], coords[2] - arrow_length
						* normal[2]);
				
				// Front11
				gl.glVertex3f(coords[0] + arrow_length * normal[0], coords[1]
						+ arrow_length * normal[1], coords[2] + arrow_length
						* normal[2]);
				gl.glVertex3f(coords[0] + stroke_length * coords[3], coords[1]
						+ stroke_length * coords[4], coords[2] + stroke_length
						* coords[5]);
				gl.glVertex3f(coords[0], coords[1], arrow_depth*coords[6]);
				
				// Front12
				gl.glVertex3f(coords[0] - arrow_length * normal[0], coords[1]
						- arrow_length * normal[1], coords[2] - arrow_length
						* normal[2]);
				gl.glVertex3f(coords[0] + stroke_length * coords[3], coords[1]
						+ stroke_length * coords[4], coords[2] + stroke_length
						* coords[5]);
				gl.glVertex3f(coords[0], coords[1], arrow_depth*coords[6]);
				
				// Front21
				gl.glVertex3f(coords[0] + arrow_length * normal[0], coords[1]
						+ arrow_length * normal[1], coords[2] + arrow_length
						* normal[2]);
				gl.glVertex3f(coords[0] + stroke_length * coords[3], coords[1]
						+ stroke_length * coords[4], coords[2] + stroke_length
						* coords[5]);
				gl.glVertex3f(coords[0], coords[1], -arrow_depth*coords[6]);
				
				// Front22
				gl.glVertex3f(coords[0] - arrow_length * normal[0], coords[1]
						- arrow_length * normal[1], coords[2] - arrow_length
						* normal[2]);
				gl.glVertex3f(coords[0] + stroke_length * coords[3], coords[1]
						+ stroke_length * coords[4], coords[2] + stroke_length
						* coords[5]);
				gl.glVertex3f(coords[0], coords[1], -arrow_depth*coords[6]);
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
	}

	private float[] dataToColor(float length) {
		float range = flow_max - flow_min;
		float value = length / range;

		float h = (1 - value);
		float s = 1;
		float b = 0.5f;

		Color hsb = Color.getHSBColor(h, s, b);

		return new float[] { hsb.getRed(), hsb.getGreen(), hsb.getBlue() };
	}
}
