import java.util.NoSuchElementException;

public class ImmutableQueue<E> {

    private final class ImmutableEmptyQueue extends ImmutableQueue<E>{  //sub class for Empty queue

        public ImmutableEmptyQueue(){                                   //Constructor for the ImmutableEmptyQueue
            super(null, null);
        }
        public ImmutableQueue<E> enqueue(E e){                          //Enqueue operation on EmptyQueue
            if(e==null){
                throw new IllegalArgumentException();                   //if no argument passed throws exception
            }
            return new ImmutableQueue<E>(forward , backward.push(e));
        }
        public ImmutableQueue<E> dequeue(){                             //Dequeue operation on EmptyQueue
            throw new NoSuchElementException();                         //throws exception
        }
        public E peek(){                                                //returns the first object
            if(this.IsEmpty()){                                         //if empty throws exception
                throw new IllegalArgumentException();
            }
            return null;
        }
        public int size(){                                              //return size zero
            return 0;
        }
        public boolean IsEmpty(){                                       //Check if empty or not
            return true;
        }

    }

    private final ImmutableStack<E> forward;                            //forward queue
    private final ImmutableStack<E> backward;                           //backward queue

    private final ImmutableEmptyQueue empty = new ImmutableEmptyQueue();//ImmutableEmptyQueue object
    public  final ImmutableQueue<E> Empty = empty;                      //ImmutableQueue object which is empty

    public ImmutableQueue(ImmutableStack<E> forward, ImmutableStack<E> backward){//Constructor of the ImmutableQueue<E> class
        this.forward = forward;
        this.backward = backward;
    }

    public ImmutableQueue<E> enqueue(E e){                              //Enqueue operation of the Immutable Queue

        if(e==null){                                                    //if no valid argumnet passed throws error
            throw new IllegalArgumentException();
        }

        return new ImmutableQueue<E>(forward , backward.push(e));       //return new Queue
    }

    public ImmutableQueue<E> dequeue(){                                 //Dequeue Operation of the Immutable Queue
        if(this.IsEmpty()){
            throw new NoSuchElementException();                         //if queue is empty throw error
        }
        ImmutableStack<E> f = forward.pop();                            //top object removed from the forward stack
        if(!f.IsEmpty()){
            return new ImmutableQueue<E>(f , backward);                 //new ImmutableQueue formed
        }
        else if(backward.IsEmpty()){
            return new ImmutableEmptyQueue();                           //new ImmutableEmptyQueue
        }
        else{

            return new ImmutableQueue<E>(backward.Reverse(),(new ImmutableStack<E>(null,null)).Empty);//new ImmutableQueue with backward stack empty
        }
    }

    private boolean IsEmpty() {
        return (forward.IsEmpty() && backward.IsEmpty());               //Checks Whether the queue is empty or not
    }

    public E peek(){
        return forward.peek();                                          //removes the first object from the ImmutableQueue
    }

    public int size(){
        return (forward.Size()+backward.Size());                        //return the size of the ImmutableQueue
    }

}

class ImmutableStack<E>{

    private final class ImmutableEmptyStack extends ImmutableStack<E> { //Immutable Empty Stack class

        public ImmutableEmptyStack() {                                  //Constructor for Immutable Empty Stack
            super(null, null);
        }
        public ImmutableStack<E> pop(){                                 //Pop Operation
            if(this.IsEmpty()){
                throw new NoSuchElementException();
            }
            return null;
        }
        public ImmutableStack<E> push(E e){                             //Push Operation

            if(e==null){
                throw new IllegalArgumentException();
            }

            return new ImmutableStack<E>(e,this);
        }
        public E peek(){                                                //Peek Operation
            if(this.IsEmpty()){
                throw new NoSuchElementException();
            }
            return null;
        }
        public boolean IsEmpty(){                                       //Checks whether any element present in the EmptyStack
            return true;
        }
        public int Size(){                                              //Size of the EmptyStack
            return 0;
        }

    }

    private final E head;                                               //head of the stack
    private final ImmutableStack<E> tail;                               //tail or the body of the stack is implemented as a stack

    private final ImmutableEmptyStack empty = new ImmutableEmptyStack();//Creating object of the ImmutableEmptyStack
    public  final ImmutableStack<E> Empty = empty;                      //Creating an EmptyImmutableStack

    public ImmutableStack(E head, ImmutableStack<E> tail){              //Constructor to build the ImmutableStack with two arguments

        this.head = head;
        this.tail = tail != null ? tail : new ImmutableEmptyStack();    //if no tail is present add the ImmutableEmptyStack as the Tail
    }

    public ImmutableStack<E> pop(){                                     //return the tail of the stack

        if(this.IsEmpty()){
            throw new NoSuchElementException();                         //if no element exists throw exception
        }

        return this.tail;
    }

    public ImmutableStack<E> push(E e){                                 //Push Operation of the Stack

        if(e==null){
            throw new IllegalArgumentException();
        }

        return new ImmutableStack<E>(e , this);
    }

    public E peek(){                                                    //return the head of the stack

        if(this.IsEmpty()){                                             //if no element exists throw exception
            throw new NoSuchElementException();
        }

        return this.head;
    }

    public boolean IsEmpty(){                                           //Check if any element exists or not in the Stack

        return (head==null && tail==null);
    }

    public int Size(){                                                  //Return the Size of the Stack

        if(IsEmpty())
            return 0;
        else
            return (1+tail.Size());
    }

    public ImmutableStack<E> Reverse(){                                 //reverse the stack and point it to the same

        ImmutableStack<E> reversedstack = new ImmutableEmptyStack();    //resultant reverse Immutable Stack initialization
        ImmutableStack<E> temp = this;                                  //temporary Immutable Stack

        while(!(temp instanceof ImmutableStack.ImmutableEmptyStack)){   //Adding the non empty instances of temp into reversedstack

            reversedstack = reversedstack.push(temp.peek());
            temp = temp.pop();
        }

        return reversedstack;

    }
}