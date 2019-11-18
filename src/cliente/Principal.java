package cliente;

import servidor.Tabuleiro;
import util.Cor;
import util.Mensagem;
import util.Status;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

public class Principal extends JFrame implements ActionListener, Runnable {
    Socket socket;
    Mensagem msg;
    Tabuleiro tabuleiro = new Tabuleiro();
    ObjectOutput output;
    ObjectInputStream input;
    JPanel painelPrincipal, painelSouth;
    private JButton btnConfirmar;
    private TextField txtfldiDestino, txtfldjOrigem, txtfldiOrigem, txtfldjDestino;
    Canvas canvas = new Canvas();
    private int id;
    Thread t1;

    public Principal() throws IOException, ClassNotFoundException {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        iniciaConexao();
        System.out.println("Sou jogador de id " + id);
        msg = new Mensagem();

        setSize(720, 550);
        setResizable(false);
        painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BorderLayout());

        painelSouth = new JPanel(new GridLayout(10, 10));

        txtfldiOrigem = new TextField("");
        painelSouth.add(new JLabel("i origem: "));
        painelSouth.add(txtfldiOrigem);

        txtfldjOrigem = new TextField("");
        painelSouth.add(new JLabel("j origem: "));
        painelSouth.add(txtfldjOrigem);

        txtfldiDestino = new TextField("");
        painelSouth.add(new JLabel("i destino: "));
        painelSouth.add(txtfldiDestino);

        txtfldjDestino = new TextField("");
        painelSouth.add(new JLabel("j destino: "));
        painelSouth.add(txtfldjDestino);

        painelSouth.add(new JLabel(""));
        btnConfirmar = new JButton("Confirmar");
        painelSouth.add(btnConfirmar);

        painelSouth.add(new JLabel(""));
        painelSouth.add(new JLabel(""));
        painelSouth.add(new JLabel(""));

        painelSouth.add(new JLabel(""));

        painelSouth.add(new JLabel(""));

        painelSouth.add(new JLabel(""));
        painelSouth.add(new JLabel(""));

        painelPrincipal.add(canvas, BorderLayout.CENTER);
        painelPrincipal.add(painelSouth, BorderLayout.EAST);

        this.setTitle("Checkers - Jogador " + id);

        Container ct = getContentPane();
        ct.add(painelPrincipal);
        btnConfirmar.addActionListener(this);

        if(id == 1) {
            t1 = new Thread(this);
            t1.start();
        }
    }

    private void iniciaConexao() throws IOException, ClassNotFoundException {
        socket = new Socket("127.0.0.1", 5000);

        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());

        Mensagem msg = (Mensagem) input.readObject();
        this.id = msg.getIdJogador();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Principal principal = new Principal();
        principal.setVisible(true);
    }

    @Override
    public void run() {
        Mensagem msg = null;
        this.btnConfirmar.setEnabled(false);
        do {
            try {
                msg = (Mensagem) input.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }while(msg.getS() != Status.OK && msg.getS() != Status.VITORIA_JOGADOR1 && msg.getS() != Status.VITORIA_JOGADOR2);

        canvas.matrizTabuleiro = msg.getTabuleiro();
        this.tabuleiro = msg.getTabuleiro();
        canvas.repaint();
        this.btnConfirmar.setEnabled(true);

        if(msg.getS().equals(Status.VITORIA_JOGADOR1)){
            if(this.id == 0){
                JOptionPane.showMessageDialog(this,"Parabéns, você venceu!");
            }else{
                JOptionPane.showMessageDialog(this,"Que pena, você perdeu :(");
            }
            this.btnConfirmar.setEnabled(false);
        } else if(msg.getS().equals(Status.VITORIA_JOGADOR2)){
            if(this.id == 0){
                JOptionPane.showMessageDialog(this,"Que pena, você perdeu :(");
            }else{
                JOptionPane.showMessageDialog(this,"Parabéns, você venceu!");
            }
            this.btnConfirmar.setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Object origem = actionEvent.getSource();
        if (origem == btnConfirmar) {
            msg = new Mensagem();
            msg.setiOrig(Integer.parseInt(txtfldiOrigem.getText()));
            msg.setjOrig(Integer.parseInt(txtfldjOrigem.getText()));
            msg.setiDest(Integer.parseInt(txtfldiDestino.getText()));
            msg.setjDest(Integer.parseInt(txtfldjDestino.getText()));
            msg.setTabuleiro(this.tabuleiro);
            msg.setIdJogador(id);
            try {
                output.writeObject(msg);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                msg = (Mensagem) input.readObject();
                canvas.matrizTabuleiro = msg.getTabuleiro();
                this.tabuleiro = msg.getTabuleiro();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if(msg.getS() == Status.JOGADA_INVALIDA){
                JOptionPane.showMessageDialog(this,"Jogada inválida");
            } else if(msg.getS() == Status.CAPTURA_OBRIGATORIA){
                JOptionPane.showMessageDialog(this,"Jogada inválida: a captura é obrigatória quando possível");
            }else if(msg.getS() == Status.PECA_ADVERSARIO){
                JOptionPane.showMessageDialog(this,"Jogada inválida: não é permitido o movimento de peças adversárias");
            }else if(msg.getS() == Status.PECA_INEXISTENTE){
                JOptionPane.showMessageDialog(this,"Jogada inválida: peça selecionada é inexistente");
            } else if(msg.getS() == Status.VITORIA_JOGADOR1){
                canvas.repaint();
                this.btnConfirmar.setEnabled(false);
                if (this.id == 0) {
                    JOptionPane.showMessageDialog(this, "Parabéns, você venceu!");
                } else {
                    JOptionPane.showMessageDialog(this, "Que pena, você perdeu :(");
                }
            } else if(msg.getS().equals(Status.VITORIA_JOGADOR2)){
                canvas.repaint();
                this.btnConfirmar.setEnabled(false);
                if(this.id == 0){
                    JOptionPane.showMessageDialog(this,"Que pena, você perdeu :(");
                }else{
                    JOptionPane.showMessageDialog(this,"Parabéns, você venceu!");
                }
            }else {

                canvas.repaint();

                t1 = new Thread(this);
                t1.start();
            }

            limpaCampos();
        }
    }

    public void limpaCampos(){
        txtfldiOrigem.setText("");
        txtfldjOrigem.setText("");
        txtfldiDestino.setText("");
        txtfldjDestino.setText("");
    }

    class Canvas extends JPanel {
        BufferedImage imagemTabuleiro, imagemPecaBranca, imagemPecaPreta, imagemPecaBrancaRei, imagemPecaPretaRei;
        Tabuleiro matrizTabuleiro = new Tabuleiro();

        public Canvas() throws IOException {
            this.matrizTabuleiro.tabuleiro = tabuleiro.tabuleiro;
            this.imagemTabuleiro = ImageIO.read(new File("tabuleiro_.png"));
            this.imagemPecaBranca = ImageIO.read(new File("dot1.png"));
            this.imagemPecaPreta = ImageIO.read(new File("dot2.png"));
            this.imagemPecaBrancaRei = ImageIO.read(new File("dot1_.png"));
            this.imagemPecaPretaRei = ImageIO.read(new File("dot2_.png"));
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(imagemTabuleiro.getScaledInstance(496, 496, 0), 0, 0, null); //desenha tabuleiro
            int x, y;

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (matrizTabuleiro.tabuleiro[i][j] == null)
                        continue;
                    if (matrizTabuleiro.tabuleiro[i][j].getCor() == Cor.PRETA) {
                        if (j == 0) {
                            x = -3;
                            y = i * 62;
                        } else if (i == 0) {
                            x = j * 62;
                            y = -3;
                        } else {
                            x = j * 62;
                            y = i * 62;
                        }

                        if (matrizTabuleiro.tabuleiro[i][j].isKing())
                            g.drawImage(imagemPecaPretaRei.getScaledInstance(62, 62, 0), x, y, null);
                        else
                            g.drawImage(imagemPecaPreta.getScaledInstance(62, 62, 0), x, y, null);
                    } else if (matrizTabuleiro.tabuleiro[i][j].getCor() == Cor.BRANCA) {
                        if (j == 0) {
                            x = -3;
                            y = i * 62;
                        } else if (i == 0) {
                            x = j * 62;
                            y = -3;
                        } else {
                            x = j * 62;
                            y = i * 62;
                        }

                        if (matrizTabuleiro.tabuleiro[i][j].isKing())
                            g.drawImage(imagemPecaBrancaRei.getScaledInstance(62, 62, 0), x, y, null);
                        else
                            g.drawImage(imagemPecaBranca.getScaledInstance(62, 62, 0), x, y, null);
                    }
                }
            }
        }
    }
}
