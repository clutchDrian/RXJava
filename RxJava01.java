import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import static java.lang.Thread.sleep;
/*import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;*/
import java.nio.file.*;
import java.util.Scanner;

/**
 *
 * @author renzo
 */
public class Semana11RxJava03 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*Programa que monitorea los cambios ocurridos en un archivo 
        de texto. Cada vez que ocurre una modificación en dicho archivo,
        el programa lanza una alerta (muestra el contenido del archivo).
        */
       
        //ruta completa del archivo que se desea monitorear
        //String ruta = "C:\\Users\\Root\\Desktop\\datos.txt"; //win
        String ruta = "C:\\Users\\renzo\\Desktop\\datos.txt";
        
        //obtenemos el directorio donde se encuentra el archivo a monitorear
        Path directorio = Paths.get(ruta).getParent();
        try{
            //Crear un watchService para monitorear los cambios en el directorio (carpeta)
            //donde se encuentra el archivo datos.txt. Estos cambios corresponden a la clase
            //FileSystems
            
            WatchService watchService = FileSystems.getDefault().newWatchService();
            
            //registrar el directorio con el WatchService para recibir los eventos de
            //notificación de modificación del archivo datos.txt
            
            directorio.register(watchService,StandardWatchEventKinds.ENTRY_MODIFY);
            
            //crear un observable que emite el contenido del archivo cada vez que este tenga modificacion
            Observable<String> archivoObservable = Observable.create(emisor->{
                //mientras el observable no se haya desechado...
                while(!emisor.isDisposed()){
                    //tomamos el siguiente evento de modificación del archivo
                    //esto sale del watchService a través del método take del objeto
                    //watchService
                    WatchKey watchKey = watchService.take();
                    
                    //recorremos los eventos de modificación de archivo:
                    for(WatchEvent<?> evento : watchKey.pollEvents()){
                        //Obtener la ruta del archivo modificado (en el directorio)
                        Path rutaActualizada = (Path)evento.context();
                        //si el archivo modificado es el que nos interesa (datos.txt)
                        if(rutaActualizada.endsWith(Paths.get(ruta).getFileName())){
                            //si esto se cumple, el cambio se ha realizado en el archivo
                            //datos.txt. Por lo tanto, leeremos el archivo y se mostrará
                            //su contenido
                            calcularImc(ruta);
                        }
                    }
                    //resetear el watchKey para recibir eventos posteriores
                    watchKey.reset();
                }
            });
            //susbribirse al observable para recibir el contenido del archivo (mi alerta)
            //cada vez que se modifica
            archivoObservable.subscribeOn(Schedulers.io());
            archivoObservable.observeOn(Schedulers.single());
            archivoObservable.subscribe(contenidoArchivo -> System.out.println(contenidoArchivo),
                    Throwable::printStackTrace,
                    () -> System.out.println("Complete")
            );

            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        try{
            sleep(Long.MAX_VALUE); //tiempo que durará el monitoreo del archivo ("infinito")
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }

    private static String obtenerPeso(String ruta) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Type your current weight: ");
    String input = scanner.nextLine();
    try {
        int peso = Integer.parseInt(input);
        try (FileWriter fileWriter = new FileWriter(ruta, true)) {
            fileWriter.write(peso + "\n");
            fileWriter.flush();
            return "Weight saved successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error saving weight: " + e.getMessage();
        }
    } catch (NumberFormatException e) {
        System.out.println("ERROR: Type a number.");
        return "Invalid input";
    }
}

    
    private static void calcularImc(String ruta) {
    System.out.println("Entering calcularImc method");
    try {
        System.out.println("Reading file contents");
        Files.lines(Paths.get(ruta)).forEach(linea -> {
            System.out.println("Processing line: " + linea);
            try {
                double peso = Double.parseDouble(linea.trim());
                // Asumo una estatura cualquiera, por ejemplo, 1.70m
                double height = 1.70;
                double imc = peso / Math.pow(height, 2);
                System.out.println("IMC calculation: " + imc);
                if (imc >= 30) {
                    System.out.println("Obesity! Your IMC is " + imc);
                } else if (imc >= 25) {
                    System.out.println("Overweight! Your IMC is " + imc);
                } else {
                    System.out.println("Normal weight! Your IMC is " + imc);
                }
            } catch (NumberFormatException e) {
                System.out.println("?" + linea);
            }
        });
    } catch (Exception e) {
        e.printStackTrace();
    }
    System.out.println("Exiting calcularImc method");
}
}