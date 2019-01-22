package mine;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

/**Refer Paper "Path planning in uncertain environment by using firefly algorithm".
 * Firefly Algorithm.
 * Used for path planning in an uncertain environment.
 * @author Welsper & Rock from SCUT.
 * @date 2019-1-12
 * @version 0.0.2
 * */

public class FireflyOptimization {
	public int numberOfFireflies = 500;//range[5,100],ө�����Ⱥ��С
	public int numberOfGenerations = 5;//range[50,100]���Ż�����������ө�����ȺǨ�ƴ���
	
	public double fittingParameterK1 = 0.8;//range[0.1,1]����Ӧϵ��K1
	public double fittingParameterK2 = 0.1;//range[0.0001,0.01] ����Ӧϵ��K2
	
	public double lightAbsorptionCoefficient = 0.1;//range[0.1,1]����������ϵ��
	public double attractiveness = 0.1;//range[0.1,1]��������
	
	public double safeRange = 0.3;//��ȫ����
	
	public Map map;//��ͼ�����ڻ���ө�����Ⱥ
	public Point2d goal = new Point2d(0,0);//Ŀ��λ��
	public Point2d center = new Point2d(0,-3);//������λ��
	
	public List<firefly> fireflies = new ArrayList<firefly>();//ө�����Ⱥ

	public int indexOfMaxLight = 0;//����ө�������
	
	public FireflyOptimization() {
		for(int i=0;i<numberOfFireflies;++i) {
			fireflies.add(new firefly());
		}
	}
	
	//��Ҫ���Ƶĵ�ͼ
	public void attachMap(Map map) {
		this.map = map;
	}
	
	/**
	 * ���ڼ���ө����ľ���
	 *@author Welsper
	 * */
	public double distanceBetweenFireflies(firefly f1,firefly f2) {
		return Math.sqrt(Math.pow(f1.position.x-f2.position.x, 2)+Math.pow(f1.position.y-f2.position.y, 2));
	}
	
	/**
	 * ���ڼ���ө������ϰ����ľ���
	 * @author Welsper
	 * */
	public double distanceBetweenFireflyAndObstacle(Point2d firefly,Point2d obstacle) {
		return Math.sqrt(Math.pow(firefly.x-obstacle.x, 2)+Math.pow(firefly.y-obstacle.y, 2));
	}
	
	//����Ŀ������
	public void setGoal(Point2d goal) {
		this.goal = goal;
	}
	
	//���û���������
	public void setCenter(Point3d center) {
		this.center.x = center.x;
		this.center.y = center.z;
	}
	
	//�������ө�����Ⱥλ��
	public void randomFirefliesPosition() {
		int id=0;
		for(firefly ff:fireflies) {
			int angle = (int)(30+Math.random()*60);
			double ratio = (double)(0.2+Math.random()*0.5);
			double x = center.x+ratio*(map.sensorFrontPoints[angle].x-center.x);
			double y = center.y+ratio*(map.sensorFrontPoints[angle].y-center.y);
			ff.setPosition(x,y);
			map.randomFireflies[id].x = x;
			map.randomFireflies[id].y = y;
			id += 1;
		}
	}
	
	//�������ө���λ��
	public Point2d nextPosition() {
		return fireflies.get(indexOfMaxLight).position;
	}
	
	//����ө�����Ⱥ������
	public void calculateLight() {
		double maxLight = 0;
		int order = 0;
		for(firefly ff:fireflies) {
			double currentFireflyLight = ff.calculateLight();
			if(currentFireflyLight>maxLight) {
				maxLight = currentFireflyLight;
				indexOfMaxLight = order;
			}
			order += 1;
		}
		map.indexOfMaxLight = indexOfMaxLight;
	}
	
	//ө�����ȺǨ��
	public void move() {
		for(firefly ff:fireflies) {
			ff.move();
		}
	}
	
	/**
	 * @author Rock
	  *  ���������ȼ�ө����λ����
	 * Firefly Algorithm�Ĺؼ�����֮һ
	 * */
	public void calcuAttraction() {
		for(int i = 0;i<numberOfFireflies;i++) {
			firefly fireflyi = fireflies.get(i);
			for(int j = 0;j<numberOfFireflies;j++) {
				firefly fireflyj = fireflies.get(j);
				if(j!=i && fireflyj.light>fireflyi.light) {
					double disj = distanceBetweenFireflies(fireflyi, fireflyj);
					double attraction = attractiveness*Math.pow(Math.E, -lightAbsorptionCoefficient*disj*disj);
					if(fireflyi.maxAttraction<attraction) {
						fireflyi.maxAttraction = attraction;
						//double alpha = Math.random()*0.9+0.1;
						double alpha = 0;//��ʱȡ�����ƫ��
						double x = attraction*(fireflyj.position.x-fireflyi.position.x)+alpha*(safeRange-0.5);
						double y = attraction*(fireflyj.position.y-fireflyi.position.y)+alpha*(safeRange-0.5);
						fireflyi.setDisplacement(x, y);
					}
				}
			}
		}
	}
	
	//ִ��ө����㷨���Ż�����
	public void faMain() {
		calculateLight();
		for(int i=0;i<numberOfGenerations;++i) {
			calcuAttraction();
			move();
			calculateLight();
		}
	}
	
	//ө�����ڲ���
	public class firefly{
		double light; //����
		double maxAttraction = 0;//���������
		Point2d position = new Point2d(0,0);//λ��
		Point2d displacement = new Point2d(0,0);//λ��
		
		/**
		  * ���㵥ֻө��������
		 * @author Welsper
		 * Firefly Algorithm�Ĺؼ�����֮һ
		 * */
		public double calculateLight() {
			double minDfo = 100;
			for(int obstacle=0;obstacle<121;++obstacle) {
				if(map.isObstacle[obstacle]) {
					double distance = distanceBetweenFireflyAndObstacle(position,map.sensorFrontPoints[obstacle]);
					if(distance<minDfo) {
						minDfo = distance;
					}
				}
			}
			double dfg = distanceBetweenFireflyAndObstacle(position,goal);
			double f = fittingParameterK1/minDfo + fittingParameterK2*dfg;
			light = Math.pow(Math.E, -f);
			return light;
		}
		
		//���õ���ө����λ��
		public void setPosition(double x,double y) {
			position.x = x;
			position.y = y;
		}
		
		//����ө����λ����
		public void setDisplacement(double x, double y) {
			this.displacement.x = x;
			this.displacement.y = y;
		}
		
		//ө����ƶ�
		public void move() {
			position.x = position.x+displacement.x;
			position.y = position.y+displacement.y;
		}
	}
	
}
