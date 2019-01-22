package mine;

import java.awt.*;
import simbad.sim.*;
import simbad.gui.Simbad;
import javax.swing.JPanel;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Point2d;
import mine.FireflyOptimization;

/**Main program of the project.
 * You need java environments with java3d and simulator simbad.
 * In fact, to add our map ui into the simulator, we have change the source code of simbad, 
 * so you need use our simbad source code as a part of your project 
 * instead of directly using the simbad jar downloaded from the official website.
 *@author Welsper from SCUT.
 *@date 2019-1-12
 *@version 0.0.1
  *��Ŀ��������
 */


public class Main {
	
	/**Robots*/
	static int numberOfRobots = 1;//����������
	static Robot[] robots = new Robot[numberOfRobots];//��������
	static Point3d[] coordinates = new Point3d[numberOfRobots];//������������
	
	/**Map*/
	static JPanel panel;//դ�񻯵�ͼ���ڵ�JPanel
	/**Grid*/
	static MapGrid mapGrid;//դ�񻯵ĵ�ͼ
	
	static Map map;//��ͼ
	
	/**FA Algorithm*/
	static FireflyOptimization ffo;//ө����㷨
	
	//դ�񻯵�ͼ���ڲ���
	static public class MapGrid{
		public int windowWidth,windowHeight;//���ڿ��
		public int worldWidth,worldHeight;//���ؿ��
		public int rowNumber,columnNumber;//դ�񻯵�ͼ��������
		public int widthInterval,heightInterval;
		public Grid[] mapGrids;//դ����
		
		public MapGrid(int windowWidth,int windowHeight,int rowNumber,int columnNumber,int worldWidth,int worldHeight) {
			this.windowWidth = windowWidth;
			this.windowHeight = windowHeight;
			this.worldWidth = worldWidth;
			this.worldHeight = worldHeight;
			this.rowNumber = rowNumber;
			this.columnNumber = columnNumber;
			widthInterval = windowWidth/columnNumber;
			heightInterval = windowHeight/rowNumber;
			mapGrids = new Grid[rowNumber*columnNumber];
		}
		
		//����������ӳ����դ������
		public Point2d mapToGridFromWorld(Point3d robotCoordinate) {
			double xGrid,yGrid;
			xGrid = (robotCoordinate.x)*(2/(double)worldWidth)*(windowWidth/2) + (windowWidth/2);
			yGrid = (robotCoordinate.z)*(2/(double)worldHeight)*(windowHeight/2) + (windowHeight/2);
			return new Point2d(xGrid,yGrid);
		}
		
		//�������˵�ǰ����դ��
		public void checkRobotPosition(Point3d robotCoordinate) {
			Point2d gridPosition = mapToGridFromWorld(robotCoordinate);
			int gridColumnNumber = (int)gridPosition.x/widthInterval;
			int gridRowNumber = (int)gridPosition.y/heightInterval;
			mapGrids[gridRowNumber*columnNumber+gridColumnNumber].ifSearch = true;
		}
		
		//��ʼ��դ����
	    public void initializeMapGrids() {
			for(int x=0;x<rowNumber;++x) {
				for(int y=0;y<columnNumber;++y) {
					float xCoordinate = y * widthInterval;
					float yCoordinate = x * heightInterval;
					mapGrids[x*columnNumber+y] = new Grid();
					mapGrids[x*columnNumber+y].topLeft = new Point2d(xCoordinate,yCoordinate);
					mapGrids[x*columnNumber+y].center = new Point2d(xCoordinate+widthInterval/2,yCoordinate+heightInterval/2);
				}
			}
		}
		
		public class Grid{
			Point2d topLeft,center;
			boolean ifSearch = false;
		}
	}

	//��ʼ����������
	static public void initializeRobots() {
		for(int index=0; index<robots.length; index++) {
			robots[index] = new Robot(new Vector3d(-5, 0, 6), "robot "+index, index);//-6
			coordinates[index] = new Point3d();
		}
	}
	
	//��ʼ����ͼ
	static public void initializeMap() {
		map = new Map(800,800,17,17);
		/*
        for(int angle = 0; angle < 360; ++angle) {
        	map.sensorPoints[angle] = new Point2d(0,0);//��ѡ������̽���������Χ360�㷶Χ���
        }
        */
        for(int angle = 0; angle < 121; ++angle) {
        	map.sensorFrontPoints[angle] = new Point2d(0,0);
        }
        for(int id=0;id<500;++id) {
        	map.randomFireflies[id] = new Point2d(0,0);
        }
	}
	
	//��ʼ��ө����㷨
	static public void initializeFA() {
    	ffo = new FireflyOptimization();
    	ffo.attachMap(map);//�󶨵�ͼ�����ڽ�ө�����Ƶ�ͼ��
    }
	
	//դ�񻯵�ͼ����JPanel���ڲ���
	static public class MapPanel extends JPanel{
		
		private static final long serialVersionUID = 1L;
		
		protected void paintComponent(Graphics g) {
            int width = 300;
            int height = 300;
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.WHITE);
            
            for(int rowNumber=0;rowNumber<30;++rowNumber) {
            	g.drawLine(0, rowNumber*10, width, rowNumber*10);
            }
            for(int columnNumber=0;columnNumber<30;++columnNumber) {
            	g.drawLine(columnNumber*10, 0, columnNumber*10, height);
            }
            
            for(int x=0;x<mapGrid.rowNumber;++x) {
            	for(int y=0;y<mapGrid.columnNumber;++y) {
            		if(mapGrid.mapGrids[x*mapGrid.columnNumber+y].ifSearch)
            			g.fillRect((int)mapGrid.mapGrids[x*mapGrid.columnNumber+y].topLeft.x, (int)mapGrid.mapGrids[x*mapGrid.columnNumber+y].topLeft.y, mapGrid.widthInterval, mapGrid.heightInterval);
            	}
            }
        }
	}
	
	//�ޱ��Ϲ��ܵ��ƶ������ˣ�����ģ���ƶ��ϰ���
    static class CircleObstacle extends Agent {
        RangeSensorBelt bumpers;

        public CircleObstacle(Vector3d position, String name) {
            super(position, name);
            bumpers = RobotFactory.addBumperBeltSensor(this, 12);
        }

        public void initBehavior() {
            // nothing particular in this case
        }

        public void performBehavior() {
        	setTranslationalVelocity(0.3);
        	if (bumpers.oneHasHit()) {
                setTranslationalVelocity(-0.1);
                setRotationalVelocity(0.5 - (0.1 * Math.random()));
            }    
        }
    }
	
    //�ڲ��࣬���Ƕ����Ѱ·������
    static public class Robot extends Agent {

    	int id;//������ID
        RangeSensorBelt sonars, bumpers;
        CameraSensor camera;
        double[] sensorAngles = new double[360];//ÿ�������������X������ĽǶ�

        public Robot(Vector3d position, String name, int id) {
            super(position, name);
            //Add camera
            camera = RobotFactory.addCameraSensor(this);
            //Add bumpers
            bumpers = RobotFactory.addBumperBeltSensor(this);
            //Add sonars
            sonars = RobotFactory.addSonarBeltSensor(this,360);
            // Attach ID
            this.id = id;
            
            for(int sensorID=0;sensorID<360;++sensorID) {
            	sensorAngles[sensorID] = sonars.getSensorAngle(sensorID);
            }
            
            //����դ�񻯵�ͼ
            if(id==0) {
            	mapGrid = new MapGrid(300,300,30,30,17,17);
            	mapGrid.initializeMapGrids();
            	
            	panel = new MapPanel();
                Dimension dim = new Dimension(300,300);
                panel.setPreferredSize(dim);
                panel.setMinimumSize(dim);
                //���դ�񻯵ĵ�ͼ
                setUIPanel(panel);
            }
            
        }
        
        /**Around 360�� range use 12 sonars sensors.*/
        public void sensorEnvironments(int angle) {
        	for(int sensorID = 0; sensorID < 12; ++sensorID) {
        		double sensorRadius = 0.0;
        		double sensorAngle = 0.0;
        		if(sonars.hasHit(sensorID)) {
        			sensorRadius = robots[this.id].radius + sonars.getMeasurement(sensorID);
        		}else {
        			sensorRadius = robots[this.id].radius + sonars.getMaxRange();
        		}
        		sensorAngle = sensorAngles[sensorID];
    			map.sensorPoints[sensorID*30 + angle].x = sensorRadius*Math.cos(sensorAngle) + coordinates[this.id].x;
    			map.sensorPoints[sensorID*30 + angle].y = -sensorRadius*Math.sin(sensorAngle) + coordinates[this.id].z;
        	}
        }
        
        /**Around 360�� range.*/
        public void sensorEnvironments() {
        	for(int sensorID = 0; sensorID < 360; ++sensorID) {
        		double sensorRadius = 0.0;
        		double sensorAngle = 0.0;
        		if(sonars.hasHit(sensorID)) {
        			sensorRadius = robots[this.id].radius + sonars.getMeasurement(sensorID);
        		}else {
        			sensorRadius = robots[this.id].radius + sonars.getMaxRange();
        		}
        		sensorAngle = sensorAngles[sensorID];
    			map.sensorPoints[sensorID].x = sensorRadius*Math.cos(sensorAngle) + coordinates[this.id].x;
    			map.sensorPoints[sensorID].y = -sensorRadius*Math.sin(sensorAngle) + coordinates[this.id].z;
        	}
        }
        
        /**Front 120�� range.
         * 
         *        60�� -> index 120
         *       /
         *      O - 0��
         *       \
         *        300�� -> index 0
         *        
         * */ 
        public boolean sensorFrontEnvironments() {
        	//���ڱ�ʶǰ��120���Ƿ�����ϰ���
        	boolean ifHit = false;
        	for(int sensorID = 0; sensorID < 360; ++sensorID) {
        		if(sonars.getSensorAngle(sensorID)>=0&&sonars.getSensorAngle(sensorID)<=Math.PI/3) {
        			double sensorRadius = 0.0;
            		double sensorAngle = 0.0;
            		if(sonars.hasHit(sensorID)) {
            			sensorRadius = robots[this.id].radius + sonars.getMeasurement(sensorID);
            			//��ʶ��ǰ�Ƕ�ǰ���Ƿ�����ϰ���
            			map.isObstacle[sensorID+60] = true;
            			ifHit = true;
            		}else {
            			sensorRadius = robots[this.id].radius + sonars.getMaxRange();
            			map.isObstacle[sensorID+60] = false;
            		}
            		sensorAngle = sensorAngles[sensorID];
        			map.sensorFrontPoints[sensorID+60].x = sensorRadius*Math.cos(sensorAngle) + coordinates[this.id].x;
        			map.sensorFrontPoints[sensorID+60].y = -sensorRadius*Math.sin(sensorAngle) + coordinates[this.id].z;
        		}
        		if(sonars.getSensorAngle(sensorID)>=5*Math.PI/3&&sonars.getSensorAngle(sensorID)<=2*Math.PI){
        			double sensorRadius = 0.0;
            		double sensorAngle = 0.0;
            		if(sonars.hasHit(sensorID)) {
            			sensorRadius = robots[this.id].radius + sonars.getMeasurement(sensorID);
            			map.isObstacle[sensorID-300] = true;
            			ifHit = true;
            		}else {
            			sensorRadius = robots[this.id].radius + sonars.getMaxRange();
            			map.isObstacle[sensorID-300] = false;
            		}
            		sensorAngle = sensorAngles[sensorID];
        			map.sensorFrontPoints[sensorID-300].x = sensorRadius*Math.cos(sensorAngle) + coordinates[this.id].x;
        			map.sensorFrontPoints[sensorID-300].y = -sensorRadius*Math.sin(sensorAngle) + coordinates[this.id].z;
        		}
        	}
        	return ifHit;
        }
        
        /**Continuous Condition
                  * ����ʹ��setTransationalVelocity�������л�������תʱ����¼���������������X��ĽǶ�
         * */
        public void updateAngle() {
        	double rotationalVelocity = getRotationalVelocity();
        	for(int sensorID=0;sensorID<360;++sensorID) {
        		sensorAngles[sensorID] += rotationalVelocity / 20;
        	}
        }
        
        /**Discrete Condition
                  * ����ʹ��rotateY�������л�������תʱ����¼���������������X��ĽǶ�
         * */
        public void updateAngle(double angle) {
        	for(int sensorID=0;sensorID<360;++sensorID) {
        		sensorAngles[sensorID] += angle;
        	}
        }
        
        public void initBehavior() {
            //nothing particular in this case
        }
        
        /**��������v2ת��v1�������ת�Ƕ�*/
        public double angleDifference(Vector2d v1,Vector2d v2) {
        	double dot = v1.x*v2.x+v1.y*v2.y;
        	double v1L = Math.sqrt(v1.x*v1.x+v1.y*v1.y);
        	double v2L = Math.sqrt(v2.x*v2.x+v2.y*v2.y);
        	double para = dot/(v1L*v2L);
        	if(para>1.0) {
        		para = 1.0;
        	}else if(para<-1.0) {
        		para = -1.0;
        	}
        	return Math.acos(para);
        }
        
        /**����v1ת��v2����ת����*/
        public boolean rotateDirection(Vector2d v1,Vector2d v2) {
        	double crossProduct = v1.x*v2.y-v2.x*v1.y;
        	if(crossProduct>0) {
        		return false;
        	}else {
        		return true;
        	}
        }
        
        /**This method is call cyclically (20 times per second)  by the simulator engine. */
        public void performBehavior() {
        	/*Get the coordinates of Robot.*/
            robots[0].getCoords(coordinates[0]);
            /*Bind Grid Map.*/
            mapGrid.checkRobotPosition(coordinates[0]);
            /*Bind Map.*/
            map.mapCanvas.attachCoorinates(coordinates[0]);
            /*Set FA Center.*/
            ffo.setCenter(coordinates[0]);    
            
            //��ײ���
            if(bumpers.hasHit(0)||bumpers.hasHit(1)||bumpers.hasHit(8)||bumpers.hasHit(2)||bumpers.hasHit(7)) {
            	setTranslationalVelocity(-0.1);
            	double randomAngle = Math.PI/2+Math.random()*Math.PI/4;
            	robots[0].rotateY(randomAngle);
            	updateAngle(randomAngle);
            }
            
            //�����˾�Ŀ����ֱ�߾���
            double drg = Math.sqrt(Math.pow(coordinates[0].x-map.goal.x, 2)+Math.pow(coordinates[0].z-map.goal.y, 2));
            
            //����Ŀ�긽��
            if(drg<1) {
            	moveToStartPosition();
            	System.out.println("Path Length = "+robots[0].odometer);
                map.mode = 2;//����Map����
            }
            
            //ÿ1sִ��һ��
            if(getCounter()%20==0) {
            	map.mode = 1;
            	if(sensorFrontEnvironments()) {
                	ffo.randomFirefliesPosition();
                	ffo.calculateLight();
                	//ffo.faMain();��ѡ���Ż�����㷨
                	Point2d next = map.randomFireflies[map.indexOfMaxLight];
                	Vector2d nextPositionVector = new Vector2d(next.x-coordinates[0].x,next.y-coordinates[0].z);
                	Vector2d currentPositionVector = new Vector2d(Math.cos(sensorAngles[0]),-Math.sin(sensorAngles[0]));
                	double angleDifference = angleDifference(nextPositionVector,currentPositionVector);
                	//ƣ�ͻ���
                	if(angleDifference < Math.PI/4) {
                		if(rotateDirection(currentPositionVector,nextPositionVector)) {
                    		robots[0].rotateY(angleDifference);
                        	updateAngle(angleDifference);
                    	}else {
                    		robots[0].rotateY(-angleDifference);
                        	updateAngle(-angleDifference);
                    	}
                	}
                	setTranslationalVelocity(0.5);
            	}else {
            		ffo.randomFirefliesPosition();
                	ffo.calculateLight();
                	//ffo.faMain();��ѡ���Ż�����㷨
                	Point2d next = map.randomFireflies[map.indexOfMaxLight];
                	Vector2d nextPositionVector = new Vector2d(next.x-coordinates[0].x,next.y-coordinates[0].z);
                	Vector2d currentPositionVector = new Vector2d(Math.cos(sensorAngles[0]),-Math.sin(sensorAngles[0]));
                	double angleDifference = angleDifference(nextPositionVector,currentPositionVector);
                	if(rotateDirection(currentPositionVector,nextPositionVector)) {
                    	robots[0].rotateY(angleDifference);
                        updateAngle(angleDifference);
                    }else {
                    	robots[0].rotateY(-angleDifference);
                        updateAngle(-angleDifference);
                    }
                	setTranslationalVelocity(0.5);
            	}
            }
            
            //ӳ������
        	map.mapCanvas.sensorFrontEnvironmentsMapToMap();             
            
            /*Paint Grid Map.*/
            panel.repaint();
            /*Paint Map.*/
            map.mapCanvas.repaint();
            /*Reset Sensor Range.*/
        }
    }

    /** Describe the environement */
    static public class MyEnv extends EnvironmentDescription {
    	
    	public void environemnt_1() {
    		//Environment-1
            Box b1 = new Box(new Vector3d(-3, 0, -3), new Vector3f(1, 1, 1),
                    this);
            add(b1);
            
            add(new Arch(new Vector3d(3, 0, -3), this));
            
            ffo.setGoal(new Point2d(3,-3));
            map.setGoal(ffo.goal);
    	}//����1
    	
    	public void environemnt_2() {
    		//Environment-2
            Box b1 = new Box(new Vector3d(-4, 0, 4), new Vector3f(12, 1, 1),
                    this);
            add(b1);
            
            Box b2 = new Box(new Vector3d(3, 0, 1), new Vector3f(12, 1, 1),
                    this);
            add(b2);
            
            Box b3 = new Box(new Vector3d(-3, 0, -3), new Vector3f(14, 1, 1),
                    this);
            add(b3);
            
            Box b4 = new Box(new Vector3d(3, 0, -6), new Vector3f(12, 1, 1),
                    this);
            add(b4);
            
            Box b5 = new Box(new Vector3d(-6, 0, -3), new Vector3f(6, 1, 1),
                    this);
            b5.rotate90(1);
            add(b5);
            
            Box b6 = new Box(new Vector3d(6, 0, -5), new Vector3f(2, 1, 1),
                    this);
            b6.rotate90(1);
            add(b6);
            
            ffo.setGoal(new Point2d(7,-7));
            //ffo.setGoal(new Point2d(-7,-5.5));Ŀ���ѡ
            map.setGoal(ffo.goal);
    	}//����2
    	
    	public void environemnt_3() {
    		Box b1 = new Box(new Vector3d(4, 0, -6), new Vector3f(10, 1, 1),
                    this);
            add(b1);
            
            Box b2 = new Box(new Vector3d(-4, 0, -3), new Vector3f(10, 1, 1),
                    this);
            add(b2);
            
            Box b3 = new Box(new Vector3d(4, 0, 1), new Vector3f(10, 1, 1),
                    this);
            add(b3);
            
            Box b4 = new Box(new Vector3d(-4, 0, 5), new Vector3f(10, 1, 1),
                    this);
            add(b4);
            
            Box b5 = new Box(new Vector3d(3, 0, -1), new Vector3f(1, 1, 1),
                    this);
            add(b5);
            
            Box b6 = new Box(new Vector3d(-4, 0, 2.5), new Vector3f(1, 1, 1),
                    this);
            add(b6);
            
            ffo.setGoal(new Point2d(7,-7));
            map.setGoal(ffo.goal);
    	}//����3
    	
    	public void environemnt_4() {
    		//Environment-2
            Box b1 = new Box(new Vector3d(-4, 0, 4), new Vector3f(12, 1, 1),
                    this);
            add(b1);
            
            Box b2 = new Box(new Vector3d(3, 0, 1), new Vector3f(12, 1, 1),
                    this);
            add(b2);
            
            Box b3 = new Box(new Vector3d(-3, 0, -3), new Vector3f(14, 1, 1),
                    this);
            add(b3);
            
            Box b4 = new Box(new Vector3d(3, 0, -6), new Vector3f(12, 1, 1),
                    this);
            add(b4);
            
            Box b5 = new Box(new Vector3d(-6, 0, -3), new Vector3f(6, 1, 1),
                    this);
            b5.rotate90(1);
            add(b5);
            
            add(new CircleObstacle(new Vector3d(2,0,-1),"circleObstacle1"));
            add(new CircleObstacle(new Vector3d(4,0,-1),"circleObstacle2"));
            
            ffo.setGoal(new Point2d(-7,-5.5));
            //ffo.setGoal(new Point2d(7,-7));Ŀ���ѡ
            map.setGoal(ffo.goal);
    	}//����4
    	
    	public void environemnt_5() {
    		//Environment-2
            Box b1 = new Box(new Vector3d(-4, 0, 4), new Vector3f(12, 1, 1),
                    this);
            add(b1);
            
            Box b2 = new Box(new Vector3d(3, 0, 1), new Vector3f(12, 1, 1),
                    this);
            add(b2);
            
            Box b3 = new Box(new Vector3d(-3, 0, -3), new Vector3f(14, 1, 1),
                    this);
            add(b3);
            
            Box b4 = new Box(new Vector3d(3, 0, -6), new Vector3f(12, 1, 1),
                    this);
            add(b4);
            
            Box b5 = new Box(new Vector3d(-6, 0, -3), new Vector3f(6, 1, 1),
                    this);
            b5.rotate90(1);
            add(b5);
            
            Box b6 = new Box(new Vector3d(6, 0, -5), new Vector3f(2, 1, 1),
                    this);
            b6.rotate90(1);
            //add(b6);
            
            ffo.setGoal(new Point2d(-7,-5.5));
            map.setGoal(ffo.goal);
    	}//����5
    	
        public MyEnv() {
            light1IsOn = true;
            light2IsOn = false;
            Wall w1 = new Wall(new Vector3d(9, 0, 0), 19, 1, this);
            w1.rotate90(1);
            add(w1);
            Wall w2 = new Wall(new Vector3d(-9, 0, 0), 19, 2, this);
            w2.rotate90(1);
            add(w2);
            Wall w3 = new Wall(new Vector3d(0, 0, 9), 19, 1, this);
            add(w3);
            Wall w4 = new Wall(new Vector3d(0, 0, -9), 19, 2, this);
            add(w4);
           
            //you can select the environments from 1 to 5.
            environemnt_5();
            
            for(int index=0; index<robots.length; index++) {
            	add(robots[index]);
            }
        }//��������
    }

    //������
    public static void main(String[] args) {
    	//initialize Map
    	initializeMap();
    	//initialize robots
    	initializeRobots();
    	//initialize Firefly Algorithms
    	initializeFA();
    	
        //request antialising
        System.setProperty("j3d.implicitAntialiasing", "true");
        //create Simbad instance with given environment
        Simbad frame = new Simbad(new MyEnv(), false);
        //������ͼ����
        frame.addMapWindow(map.createMapUI());
    }

} 
