package src.engine.core.rendering.SimpleRenderPipeline;

public class DrawingThread extends Thread{

        public volatile boolean run = false;

        private ObjectBuffer objectBuffer;

        public DrawingThread(ObjectBuffer objectBuffer){
            this.objectBuffer = objectBuffer;

            this.start();

        }
        @Override
        public void run(){
            while (true){
                if(run){
                    int size = objectBuffer.trianglesToRender.size();

                    for(int i = 0; i < size; i++){

                        objectBuffer.drawingWindow.drawTriangle(objectBuffer.trianglesToRender.get(i));
                    }

                    run = false;
                }
            }
        }
}
