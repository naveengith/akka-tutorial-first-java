import akka.actor.*;

/**
 * Created by naveen on 1/11/17.
 */
public class Division {

//  Create massages
    static class Calculate {
    }

    static class Work {
        private final int nume;
        private final int denom;
        private final ActorRef context;

        public Work(int nume, int denom,ActorRef context) {
            this.nume = nume;
            this.denom = denom;
            this.context = context;
        }

        public int getNume() {
            return nume;
        }

        public int getDenom() {
            return denom;
        }

        public ActorRef getContext(){ return context; }
    }

    static class Result {
        private final double value;
        private final ActorRef context;

        public Result(double value,ActorRef context) {
            this.value = value;
            this.context = context;
        }

        public double getValue() {
            return value;
        }

        public  ActorRef getContext(){ return context; }
    }

    static class Calculation {
        private final double finalResult;
        private final String status;

        public Calculation(double finalResult, String status) {
            this.finalResult = finalResult;
            this.status = status;
        }

        public double getFinalResult() {
            return finalResult;
        }

        public String getStatus() {
            return status;
        }
    }
// Create Actors
    public static class Worker extends UntypedActor {

        private double calculateDevFor(double nume, double denom) {
            double acc =0.0 ;
            acc = nume / denom;
            return acc;
        }

        public void onReceive(Object message) {
            if (message instanceof Work) {
                Work work = (Work) message;
                System.out.println("Numerator "+work.getNume());
                System.out.println("Denominator "+work.getDenom());
                double result = calculateDevFor(work.getNume(), work.getDenom());

                getSender().tell(new Result(result,work.getContext()), getSelf());
            } else {
                unhandled(message);
            }
        }
    }

    public static class WorkerMul extends UntypedActor {

        private double calculateDevFor(double nume, double denom) {
            double acc =0.0 ;
            acc = nume * denom;
            return acc;
        }

        public void onReceive(Object message) {
            if (message instanceof Work) {
                Work work = (Work) message;
                System.out.println("Numerator "+work.getNume());
                System.out.println("Denominator "+work.getDenom());
                double result = calculateDevFor(work.getNume(), work.getDenom());

                getSender().tell(new Result(result,work.getContext()), getSelf());
            } else {
                unhandled(message);
            }
        }
    }

    public static class User extends UntypedActor {
        private final int numerator;
        private final int denomenator;
        private final ActorRef master;

        public User(final int numerator, int denomenator, ActorRef master) {
            this.numerator = numerator;
            this.denomenator = denomenator;
            this.master = master;
        }

        public void onReceive(Object message) {
            if (message instanceof Calculate) {
                ActorRef context = getSelf();
                master.tell(new Work(numerator, denomenator,context), getSelf());
            } else if (message instanceof Calculation) {
                Calculation calculation = (Calculation) message;

                System.out.println(String.format("\n\tCalculation: \t%s\n\tStatus : \t%s",
                        calculation.getFinalResult(), calculation.getStatus()));
                getContext().system().shutdown();
            } else {
                unhandled(message);
            }
        }
    }

    public static class Master extends UntypedActor {

        private final ActorRef worker;
        private final ActorRef workerMul;

        public Master(){
            worker = this.getContext().actorOf(new Props(Worker.class),"worker");
            workerMul = this.getContext().actorOf(new Props(WorkerMul.class),"workerMul");
        }

        private double devFor(int nume, int denom) {
            double acc = 1.0;
            if (denom <= 0) {
                acc = 0;
            }
            return acc;
        }

        public void onReceive(Object message) {
            if (message instanceof Work) {
                Work work = (Work) message;
                double result = devFor(work.getNume(), work.getDenom());
                if (result == 0) {

                    getSender().tell(new Calculation(result,"Can't Devide by Zero"), getSelf());
                } else{
                    worker.tell(new Work(work.getNume(), work.getDenom(),work.getContext()), getSelf());
                    workerMul.tell(new Work(work.getNume(), work.getDenom(),work.getContext()), getSelf());
                }
            } else if (message instanceof Result) {

                Result result = (Result) message;

                System.out.println("Worker calculate : "+result.getValue());
                result.getContext().tell(new Calculation(result.getValue(),"Success"),getSelf());

            } else {
                unhandled(message);
            }
        }
    }
// end of actors
        public void calculate(final int numerator, final int denomerator) {

        // Create an Akka system
            ActorSystem system = ActorSystem.create("devSystem");

            final ActorRef master = system.actorOf(new Props(Master.class), "master");

            // create the user
            ActorRef user = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new User(numerator, denomerator, master);
                }
            }), "user");

            // start the calculation
            user.tell(new Calculate());
        }
    }
