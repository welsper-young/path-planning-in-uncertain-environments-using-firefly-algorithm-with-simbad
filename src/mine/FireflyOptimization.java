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
	public int numberOfFireflies = 500;//range[5,100],萤火虫族群大小
	public int numberOfGenerations = 5;//range[50,100]，优化迭代次数，萤火虫族群迁移次数
	
	public double fittingParameterK1 = 0.8;//range[0.1,1]，适应系数K1
	public double fittingParameterK2 = 0.1;//range[0.0001,0.01] ，适应系数K2
	
	public double lightAbsorptionCoefficient = 0.1;//range[0.1,1]，亮度吸收系数
	public double attractiveness = 0.1;//range[0.1,1]，吸引度
	
	public double safeRange = 0.3;//安全距离
	
	public Map map;//地图，用于绘制萤火虫族群
	public Point2d goal = new Point2d(0,0);//目标位置
	public Point2d center = new Point2d(0,-3);//机器人位置
	
	public List<firefly> fireflies = new ArrayList<firefly>();//萤火虫族群

	public int indexOfMaxLight = 0;//最亮萤火虫索引
	
	public FireflyOptimization() {
		for(int i=0;i<numberOfFireflies;++i) {
			fireflies.add(new firefly());
		}
	}
	
	//绑定要绘制的地图
	public void attachMap(Map map) {
		this.map = map;
	}
	
	/**
	 * 用于计算萤火虫间的距离
	 *@author Welsper
	 * */
	public double distanceBetweenFireflies(firefly f1,firefly f2) {
		return Math.sqrt(Math.pow(f1.position.x-f2.position.x, 2)+Math.pow(f1.position.y-f2.position.y, 2));
	}
	
	/**
	 * 用于计算萤火虫与障碍物间的距离
	 * @author Welsper
	 * */
	public double distanceBetweenFireflyAndObstacle(Point2d firefly,Point2d obstacle) {
		return Math.sqrt(Math.pow(firefly.x-obstacle.x, 2)+Math.pow(firefly.y-obstacle.y, 2));
	}
	
	//设置目标坐标
	public void setGoal(Point2d goal) {
		this.goal = goal;
	}
	
	//设置机器人坐标
	public void setCenter(Point3d center) {
		this.center.x = center.x;
		this.center.y = center.z;
	}
	
	//随机生成萤火虫族群位置
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
	
	//获得最亮萤火虫位置
	public Point2d nextPosition() {
		return fireflies.get(indexOfMaxLight).position;
	}
	
	//计算萤火虫族群的亮度
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
	
	//萤火虫族群迁徙
	public void move() {
		for(firefly ff:fireflies) {
			ff.move();
		}
	}
	
	/**
	 * @author Rock
	  *  计算吸引度及萤火虫的位移量
	 * Firefly Algorithm的关键步骤之一
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
						double alpha = 0;//暂时取消随机偏移
						double x = attraction*(fireflyj.position.x-fireflyi.position.x)+alpha*(safeRange-0.5);
						double y = attraction*(fireflyj.position.y-fireflyi.position.y)+alpha*(safeRange-0.5);
						fireflyi.setDisplacement(x, y);
					}
				}
			}
		}
	}
	
	//执行萤火虫算法的优化迭代
	public void faMain() {
		calculateLight();
		for(int i=0;i<numberOfGenerations;++i) {
			calcuAttraction();
			move();
			calculateLight();
		}
	}
	
	//萤火虫的内部类
	public class firefly{
		double light; //亮度
		double maxAttraction = 0;//最大吸引度
		Point2d position = new Point2d(0,0);//位置
		Point2d displacement = new Point2d(0,0);//位移
		
		/**
		  * 计算单只萤火虫的亮度
		 * @author Welsper
		 * Firefly Algorithm的关键步骤之一
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
		
		//设置单个萤火虫的位置
		public void setPosition(double x,double y) {
			position.x = x;
			position.y = y;
		}
		
		//设置萤火虫的位移量
		public void setDisplacement(double x, double y) {
			this.displacement.x = x;
			this.displacement.y = y;
		}
		
		//萤火虫移动
		public void move() {
			position.x = position.x+displacement.x;
			position.y = position.y+displacement.y;
		}
	}
	
}
