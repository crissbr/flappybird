package com.cursoandroid.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Game extends ApplicationAdapter {

	//Texturas
	private SpriteBatch batch;
	private Texture [] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	//formas p/colisao
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle rectangleCanoCima;
	private Rectangle rectangleCanoBaixo;

	//Atributos de configuracao
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaohorizontalPassaro = 0;

	//Exibicao de textos
	BitmapFont textoPontucao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontucao;

	//Config Sons
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	//Objeto salvar pontuacao
	Preferences preferences;

	//Objeto para Camara
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT =1280;
	
	@Override
	public void create () {

		inicializarObjetosTela();
		inicializarObjeto();
	}

	@Override
	public void render () {

		//limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();

	}

	private void verificarEstadoJogo(){

		boolean toqueTela = Gdx.input.justTouched();

		if (estadoJogo == 0){
			//Aplica evento de toque na tela

			if (toqueTela){
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}

		}else if(estadoJogo ==1){

			if (toqueTela) {
				gravidade = -15;
				somVoando.play();
			}

			//Movimentar o cano
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime()*200;
			if (posicaoCanoHorizontal< - canoTopo.getWidth()){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) -200;
				passouCano = false;
			}

			//Aplica gravidade passaro 500
			if (posicaoInicialVerticalPassaro > 0 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			variacao += Gdx.graphics.getDeltaTime() *10;

			//Verifica variacao p/bater asas do pássaro
			if (variacao > 3)
				variacao = 0;
			gravidade++;

		}else if(estadoJogo ==2){

			if (pontos > pontuacaoMaxima){
				pontuacaoMaxima = pontos;
				preferences.putInteger("pontucaoMaxima", pontuacaoMaxima);

			}
			posicaohorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;

         //aplica evento toque na tela
			if (toqueTela) {
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaohorizontalPassaro = 0;
				posicaoInicialVerticalPassaro =alturaDispositivo/2;
				posicaoCanoHorizontal =larguraDispositivo;
			}
		}
	}

	public void validarPontos(){

		if (posicaoCanoHorizontal < 50 - passaros[0].getWidth()){//passou da posicao do passaro
			if (!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}
        //Bater asas passaro
		variacao += Gdx.graphics.getDeltaTime() *10;
		if (variacao>3){
			variacao = 0;
		}
	}

	private void detectarColisoes(){

		circuloPassaro.set(50 +posicaohorizontalPassaro+ passaros[0].getWidth()/2 ,posicaoInicialVerticalPassaro +passaros[0].getHeight()/2,passaros[0].getWidth()/2

		);

		rectangleCanoBaixo.set(posicaoCanoHorizontal,alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight());
		rectangleCanoCima.set(posicaoCanoHorizontal,alturaDispositivo/2 +espacoEntreCanos/2 +posicaoCanoVertical,
				canoTopo.getWidth(),canoTopo.getHeight());

		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, rectangleCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, rectangleCanoBaixo);

		if (colidiuCanoCima || colidiuCanoBaixo){
			if (estadoJogo ==1){
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}

	private void desenharTexturas(){
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		batch.draw(fundo,0,0,larguraDispositivo ,alturaDispositivo );
		batch.draw(passaros [(int) variacao], 50 + posicaohorizontalPassaro, posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo,posicaoCanoHorizontal,alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical);
		textoPontucao.draw(batch, String.valueOf(pontos), larguraDispositivo/2,alturaDispositivo - 110);
		batch.draw(canoTopo,posicaoCanoHorizontal,alturaDispositivo/2 +espacoEntreCanos/2 +posicaoCanoVertical);

		if (estadoJogo ==2){
			batch.draw(gameOver, larguraDispositivo/2 - gameOver.getWidth()/2, alturaDispositivo/2);
			textoReiniciar.draw(batch,"Toque para Reiniciar!",larguraDispositivo/2-140, alturaDispositivo/2- gameOver.getHeight()/2);
			textoMelhorPontucao.draw(batch, "Seu record é: " +pontuacaoMaxima + "pontos",larguraDispositivo/2-140,alturaDispositivo/2- gameOver.getHeight());
		}

		batch.end();
	}

	private void inicializarObjetosTela(){
		passaros = new Texture[3];
		passaros [0] = new Texture("passaro1.png");
		passaros [1] = new Texture("passaro2.png");
		passaros [2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");

	}

	private void inicializarObjeto(){
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo/2;
		posicaoCanoHorizontal =larguraDispositivo;
		espacoEntreCanos = 350;

		//Config dos textos
		textoPontucao = new BitmapFont();
		textoPontucao.setColor(Color.WHITE);
		textoPontucao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontucao = new BitmapFont();
		textoMelhorPontucao.setColor(Color.RED);
		textoMelhorPontucao.getData().setScale(2);

		//Formas geometricas p/colisoes
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		rectangleCanoCima = new Rectangle();
		rectangleCanoBaixo = new Rectangle();

		//Inicia Sons
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		//Config preferncias do objeto
		preferences = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima =preferences.getInteger("pontuacaoMaxima",0);

		//Config Camera
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT,camera);

	}

	@Override
	public void resize(int width, int height) {

		viewport.update(width, height);
	}

	@Override
	public void dispose () {

	}
}
