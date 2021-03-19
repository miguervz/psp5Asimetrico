package es.studium.pspasimetrico;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

public class HiloServidor extends Thread
{
	DataInputStream fentrada;
	Socket socket;
	boolean fin = false;
	String encrip, desencrip;
	Vector<String> lineas = new Vector<String>();
	
	public HiloServidor(Socket socket)
	{
		this.socket = socket;
		try
		{
			fentrada = new DataInputStream(socket.getInputStream());
		} catch (IOException e)
		{
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}

	// En el m�todo run() lo primero que hacemos
	// es enviar todos los mensajes actuales al cliente que se
	// acaba de incorporar
	public void run()
	{
		ServidorChat.mensaje.setText("N�mero de conexiones actuales: " + ServidorChat.ACTUALES);
		String texto = ServidorChat.textarea.getText();
		EnviarMensajes(texto);
		// Seguidamente, se crea un bucle en el que se recibe lo que el cliente escribe en el chat.
		// Cuando un cliente finaliza con el bot�n Salir, se env�a un * al servidor del Chat,
		// entonces se sale del bucle while, ya que termina el proceso del cliente,
		// de esta manera se controlan las conexiones actuales
		while (!fin)
		{
			String cadena = "";
			try
			{
				cadena = fentrada.readUTF();
				desencrip = desencriptado(cadena);
				System.out.println("Encriptado: "+cadena);
				System.out.println("Desencriptado: "+desencrip);
				if (desencrip.trim().equals("*"))
				{
					ServidorChat.ACTUALES--;
					ServidorChat.mensaje.setText("N�mero de conexiones actuales: " + ServidorChat.ACTUALES);
					fin = true;
				}
				// El texto que el cliente escribe en el chat,
				// se a�ade al textarea del servidor y se reenv�a a todos los clientes
				else
				{
					 ServidorChat.textarea.append(desencrip + "\n");
						String [] lineas2 = ServidorChat.textarea.getText().split("\n");
						for(int i=0;i<lineas2.length;i++){
							 String linea = lineas2[i];
							 lineas.add(linea);
					        }
						if(lineas.lastElement() != null) {
							EnviarMensajes(lineas.lastElement());
						}
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
				fin = true;
			}
		}
	}

	// El m�todo EnviarMensajes() env�a el texto del textarea a
	// todos los sockets que est�n en la tabla de sockets,
	// de esta forma todos ven la conversaci�n.
	// El programa abre un stream de salida para escribir el texto en el socket
	private void EnviarMensajes(String texto)
	{
		for (int i = 0; i < ServidorChat.CONEXIONES; i++)
		{
			Socket socket = ServidorChat.tabla[i];
			try
			{
				DataOutputStream fsalida = new DataOutputStream(socket.getOutputStream());
				encrip = encriptado(texto);
				fsalida.writeUTF(encrip);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	public String desencriptado(String texto) {
		String desencriptado = "";
		// Trabajamos con las claves privadas y p�blicas
		try {
			RSA rsaCliente = new RSA();
			rsaCliente.genKeyPair(512);
			rsaCliente.openFromDiskPrivateKey("rsaCliente.pri");
			rsaCliente.openFromDiskPublicKey("rsaCliente.pub");
			desencriptado = rsaCliente.Decrypt(texto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return desencriptado;
	}
	public String encriptado(String texto) {
		String encriptado = "";
		// Trabajamos con las claves privadas y p�blicas
		try {
			RSA rsaServidor = new RSA();
			rsaServidor.genKeyPair(512);
			rsaServidor.saveToDiskPrivateKey("rsaServidor.pri");
			rsaServidor.saveToDiskPublicKey("rsaServidor.pub");
			encriptado = rsaServidor.Encrypt(texto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encriptado;
	}
}