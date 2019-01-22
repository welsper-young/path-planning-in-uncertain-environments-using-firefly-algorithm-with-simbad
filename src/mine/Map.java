package mine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Canvas;
import javax.swing.JInternalFrame;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

/** Used for painting map.
 * @author Welsper from SCUT.
 * @date 2019-1-10
 * @version 0.0.2
  * ���ڻ��Ƶ�ͼ
 * */

public class Map{
	public MapCanvas mapCanvas;//����
	public JInternalFrame window;//�ڲ�����
	
	public Point2d[] sensorPoints = new Point2d[360];//̽����Χ360�㷶Χ�ڵ����
	public int[] sensorPointsX = new int[361];//���ڻ�ͼ��X������
	public int[] sensorPointsY = new int[361];//���ڻ�ͼ��Y������
	
	public int mode = 0;//���Ʒ��������0��1��2
	
	public Point2d[] sensorFrontPoints = new Point2d[121];//̽�������ǰ��120�㷶Χ�ڵ����
	public boolean[] isObstacle = new boolean[121];//ǰ��120�㷶Χ�Ƿ�Ϊ�ϰ���
	public int[] sensorFrontPointsX = new int[123];//���ڻ�ͼ��X������
	public int[] sensorFrontPointsY = new int[123];//���ڻ�ͼ��Y������
	
	public Point2d[] randomFireflies = new Point2d[500];//���ө�����Ⱥ
	public int indexOfMaxLight = 0;//������ө�������
	
	public Point2d goal = new Point2d(0,0);//Ŀ��λ��
	
	public Map(int windowWidth,int windowHeight,int worldWidth,int worldHeight) {
		mapCanvas = new MapCanvas(windowWidth,windowHeight,worldWidth,worldHeight);
		Dimension dim = new Dimension(windowWidth,windowHeight);
		mapCanvas.setSize(dim);
		mapCanvas.setBackground(Color.black);
	}
	
	public JInternalFrame createMapUI(){
		window = new JInternalFrame("Map",false,false,false,false);
		window.add(mapCanvas);
        window.pack();
        window.setVisible(true);
        return window;
	}
	
	//����Ŀ������
	public void setGoal(Point2d goal) {
		this.goal = goal;
	}
	
	//����
	public final class MapCanvas extends Canvas{
		private static final long serialVersionUID = 1L;
		
		public int windowWidth,windowHeight;
		public int worldWidth,worldHeight;
		public Point2d coordinate = new Point2d(0,-3);
		
		public MapCanvas(int windowWidth,int windowHeight,int worldWidth,int worldHeight) {
			this.windowHeight = windowHeight;
			this.windowWidth = windowWidth;
			this.worldHeight = worldHeight;
			this.worldWidth = worldWidth;
		}
		
		//�󶨻���������
		public void attachCoorinates(Point3d coordinates) {
			double xGrid,yGrid;
			xGrid = (coordinates.x)*(2/(double)worldWidth)*(windowWidth/2) + (windowWidth/2);
			yGrid = (coordinates.z)*(2/(double)worldHeight)*(windowHeight/2) + (windowHeight/2);
			coordinate.x = xGrid;
			coordinate.y = yGrid;
		}
		
		//��World����ӳ��Ϊ��������
		public void sensorEnvironmentsMapToMap() {
			for(int id=0;id<360;++id) {
				sensorPointsX[id] = (int) ((sensorPoints[id].x)*(2/(double)worldWidth)*(windowWidth/2) + (windowWidth/2));
				sensorPointsY[id] = (int) ((sensorPoints[id].y)*(2/(double)worldHeight)*(windowHeight/2) + (windowHeight/2));
			}
			sensorPointsX[360] = sensorPointsX[0];
			sensorPointsY[360] = sensorPointsY[0];
		}
		
		//��World����ӳ��Ϊ��������
		public void sensorFrontEnvironmentsMapToMap() {
			for(int id=0;id<121;++id) {
				sensorFrontPointsX[id+1] = (int) ((sensorFrontPoints[id].x)*(2/(double)worldWidth)*(windowWidth/2) + (windowWidth/2));
				sensorFrontPointsY[id+1] = (int) ((sensorFrontPoints[id].y)*(2/(double)worldHeight)*(windowHeight/2) + (windowHeight/2));
			}
			sensorFrontPointsX[0] = (int)coordinate.x;
			sensorFrontPointsY[0] = (int)coordinate.y;
			sensorFrontPointsX[122] = (int)coordinate.x;
			sensorFrontPointsY[122] = (int)coordinate.y;
		}
		
		//���ƻ���
		public void update(Graphics g) {
			switch(mode) {
			case 0:{
			}break;
			case 1:{
				g.setColor(Color.white);
				//����ǰ��120��̽�ⷶΧ
				g.fillPolygon(sensorFrontPointsX,sensorFrontPointsY,123);
				//����ө�����Ⱥ
				for(int id=0;id<500;++id) {
					int x = (int) ((randomFireflies[id].x)*(2/(double)worldWidth)*(windowWidth/2) + (windowWidth/2));
					int y = (int) ((randomFireflies[id].y)*(2/(double)worldWidth)*(windowWidth/2) + (windowWidth/2));
					if(id==indexOfMaxLight) {
						g.setColor(Color.red);
						g.fillOval(x,y,5,5);
					}else {
						g.setColor(Color.orange);
						g.fillOval(x,y,3,3);
					}
				}
				g.setColor(Color.green);
				//����Ŀ���
				g.fillOval((int)((goal.x)*(2/(double)worldWidth)*(windowWidth/2) + (windowWidth/2)),(int)((goal.y)*(2/(double)worldWidth)*(windowWidth/2) + (windowWidth/2)),10,10);
				mode = 0;
			}break;
			case 2:{
				//����ģʽ-2������Map����
				g.setColor(Color.black);
				g.fillRect(0, 0, this.windowWidth, this.windowHeight);
				mode = 0;
			}break;
			}
		}
	}
}
