/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.zrp200.rkpd2.sprites;

import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.Phantom;
import com.zrp200.rkpd2.effects.DarkBlock;
import com.zrp200.rkpd2.effects.EmoIcon;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.IceBlock;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.ShieldHalo;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.effects.StoneBlock;
import com.zrp200.rkpd2.effects.TorchHalo;
import com.zrp200.rkpd2.effects.particles.FlameParticle;
import com.zrp200.rkpd2.effects.particles.FrostfireParticle;
import com.zrp200.rkpd2.effects.particles.GodfireParticle;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.effects.particles.SnowParticle;
import com.zrp200.rkpd2.effects.particles.VineParticle;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.ui.CharHealthIndicator;

import java.nio.Buffer;

public class CharSprite extends MovieClip implements Tweener.Listener, MovieClip.Listener {

	// Color constants for floating text
	public static final int DEFAULT		= 0xFFFFFF;
	public static final int POSITIVE	= 0x00FF00;
	public static final int NEGATIVE	= 0xFF0000;
	public static final int WARNING		= 0xFF8800;
	public static final int NEUTRAL		= 0xFFFF00;

	public static final float DEFAULT_MOVE_INTERVAL = 0.1f;
	private static float moveInterval = DEFAULT_MOVE_INTERVAL;
	private static final float FLASH_INTERVAL	= 0.05f;

	//the amount the sprite is raised from flat when viewed in a raised perspective
	protected float perspectiveRaise    = 6 / 16f; //6 pixels

	//the width and height of the shadow are a percentage of sprite size
	//offset is the number of pixels the shadow is moved down or up (handy for some animations)
	protected boolean renderShadow  = false;
	protected float shadowWidth     = 1.2f;
	protected float shadowHeight    = 0.25f;
	protected float shadowOffset    = 0.25f;

	public enum State {
		BURNING, GODBURNING, LEVITATING, INVISIBLE, PARALYSED, FROZEN, ILLUMINATED, CHILLED, DARKENED, MARKED, HEALING, SHIELDED, FROSTBURNING, SPIRIT, SHRUNK, ALLURED, ENLARGENED, AURA, SWORDS, STONED, HEARTS, WARPED, VINECOVERED
	}
	private int stunStates = 0;

	protected Animation idle;
	protected Animation run;
	protected Animation attack;
	protected Animation operate;
	protected Animation zap;
	protected Animation die;
protected void copyAnimations(CharSprite other) {
		idle = other.idle;
		run = other.run;
		attack = other.attack;
		operate = other.operate;
		zap = other.zap;
		die = other.die;
	}

	protected Callback animCallback;

	protected PosTweener motion;

	protected Emitter burning;
	protected Emitter red_burning;
	protected Emitter chilled;
	protected Emitter marked;
	protected Emitter levitation;
	protected Emitter healing;
	protected Emitter hearts;
	protected Emitter frostburning;
	protected Emitter spirit;
	protected Emitter allured;
	protected Emitter warped;
	protected Emitter vines;
	public Emitter swords;

	protected IceBlock iceBlock;
	protected DarkBlock darkBlock;
	protected TorchHalo light;
	protected ShieldHalo shield;
	protected AlphaTweener invisible;
	protected StoneBlock stone;
	protected Flare aura;
	
	protected EmoIcon emo;
	protected CharHealthIndicator health;

	private Tweener jumpTweener;
	private Callback jumpCallback;

	protected float flashTime = 0;

	protected boolean sleeping = false;

	public Char ch;

	//used to prevent the actor associated with this sprite from acting until movement completes
	public volatile boolean isMoving = false;

	public CharSprite() {
		super();
		listener = this;
	}

	@Override
	public void play(Animation anim) {
		//Shouldn't interrupt the dieing animation
		if (curAnim == null || curAnim != die) {
			super.play(anim);
		}
	}


	//intended to be used for placing a character in the game world
	public void link( Char ch ) {
		linkVisuals( ch );

		this.ch = ch;
		ch.sprite = this;

		place( ch.pos );
		turnTo( ch.pos, Random.Int( Dungeon.level.length() ) );
		renderShadow = true;

		if (ch != Dungeon.hero) {
			if (health == null) {
				health = new CharHealthIndicator(ch);
			} else {
				health.target(ch);
			}
		}

		ch.updateSpriteState();
	}
@Override
	public void destroy() {
		super.destroy();
		if (ch != null && ch.sprite == this){
			ch.sprite = null;
		}
	}
	//used for just updating a sprite based on a given character, not linking them or placing in the game
	public void linkVisuals( Char ch ){
		if (ch instanceof Mob){
			scale.set(((Mob) ch).scaleFactor);
			place(ch.pos);
		}
	}

	public PointF worldToCamera( int cell ) {

		final int csize = DungeonTilemap.SIZE;

		return new PointF(
				PixelScene.align(Camera.main, ((cell % Dungeon.level.width()) + 0.5f) * csize - width() * 0.5f),
				PixelScene.align(Camera.main, ((cell / Dungeon.level.width()) + 1.0f) * csize - height() - csize * perspectiveRaise)
		);
	}

	public void place( int cell ) {
		point( worldToCamera( cell ) );
	}

	public void showStatus( int color, String text, Object... args ) {
		showStatusWithIcon(color, text, FloatingText.NO_ICON, args);
	}

	public void showStatusWithIcon( int color, String text, int icon, Object... args ) {
		if (visible) {
			if (args.length > 0) {
				text = Messages.format( text, args );
			}
			float x = destinationCenter().x;
			float y = destinationCenter().y - height()/2f;
			if (ch != null) {
				FloatingText.show( x, y, ch.pos, text, color, icon, true );
			} else {
				FloatingText.show( x, y, -1, text, color, icon, true );
			}
		}
	}

	public void idle() {
		play(idle);
	}

	public void move( int from, int to ) {
		turnTo( from , to );

		play( run );

		motion = new PosTweener( this, worldToCamera( to ), moveInterval );
		motion.listener = this;
		parent.add( motion );

		isMoving = true;

		if (visible && ch.isWet() && !ch.flying) {
			GameScene.ripple( from );
		}

	}

	public static void setMoveInterval( float interval){
		moveInterval = interval;
	}

	//returns where the center of this sprite will be after it completes any motion in progress
	public PointF destinationCenter(){
		PosTweener motion = this.motion;
		if (motion != null && motion.elapsed >= 0){
			return new PointF(motion.end.x + width()/2f, motion.end.y + height()/2f);
		} else {
			return center();
		}
	}

	public void interruptMotion() {
		if (motion != null) {
			motion.stop(false);
		}
	}

	// putting animation changes in here will guarentee that each gets to play, but the default is that they override each other.
	// didn't expect to have to rewrite this in order to get what I want...
	// set is used to emulate current behavior vs the delayed behavior.
	// note that doing this also overrides auto-ending so you might want to deal with that.
	public void doAfterAnim(Callback callback) { doAfterAnim(callback,false); }
	protected void doAfterAnim(Callback callback, boolean set) {
		if(animCallback != null) { // which means something is playing
			Callback curCallback = animCallback;
			animCallback = () -> { // this allows me to effectively stack callbacks while keeping sequence of events intact.
				curCallback.call();
				callback.call();
			};
		}
		else if (set) animCallback = callback; // this is the internal behavior.
		else callback.call(); // this is the public behavior.
	}

	public synchronized void attack( int cell ) {
		turnTo( ch.pos, cell );
		play( attack );
	}

	public synchronized void attack( int cell, Callback callback ) {
		doAfterAnim(callback,true);
		attack(cell);
	}

	public synchronized void operate( int cell ) {
		turnTo( ch.pos, cell );
		play( operate );
	}

	public final synchronized void operate( int cell, Callback callback ) {
		doAfterAnim(callback,true);
		operate(cell);
	}

	public synchronized void zap( int cell ) {
		turnTo( ch.pos, cell );
		play( zap );
	}

	public final synchronized void zap( int cell, Callback callback ) {
		doAfterAnim(callback,true);
		zap( cell );
	}

	public void turnTo( int from, int to ) {
		int fx = from % Dungeon.level.width();
		int tx = to % Dungeon.level.width();
		if (tx > fx) {
			flipHorizontal = false;
		} else if (tx < fx) {
			flipHorizontal = true;
		}
	}

	public void jump( int from, int to, Callback callback ) {
		float distance = Math.max( 1f, Dungeon.level.trueDistance( from, to ));
		jump( from, to, distance * 2, distance * 0.1f, callback );
	}

	public void jump( int from, int to, float height, float duration,  Callback callback ) {
		jumpCallback = callback;

		jumpTweener = new JumpTweener( this, worldToCamera( to ), height, duration );
		jumpTweener.listener = this;
		parent.add( jumpTweener );

		turnTo( from, to );
	}

	public void die() {
		sleeping = false;
		play( die );

		hideEmo();

		if (health != null){
			health.killAndErase();
		}
	}

	public Emitter emitter() {
		Emitter emitter = GameScene.emitter();
		if (emitter != null) emitter.pos( this );
		return emitter;
	}

	public Emitter centerEmitter() {
		Emitter emitter = GameScene.emitter();
		if (emitter != null) emitter.pos( center() );
		return emitter;
	}

	public Emitter bottomEmitter() {
		Emitter emitter = GameScene.emitter();
		if (emitter != null) emitter.pos( x, y + height, width, 0 );
		return emitter;
	}

	public void burst( final int color, int n ) {
		if (visible) {
			Splash.at( center(), color, n );
		}
	}

	public void bloodBurstA( PointF from, int damage ) {
		if (visible) {
			PointF c = center();
			int n = (int)Math.min( 9 * Math.sqrt( (double)damage / ch.HT ), 9 );
			Splash.at( c, PointF.angle( from, c ), 3.1415926f / 2, blood(), n );
		}
	}

	public int blood() {
		return 0xFFBB0000;
	}

	public void flash() {
		ra = ba = ga = 1f;
		flashTime = FLASH_INTERVAL;
	}

	public void add( State state ) {
		switch (state) {
			case BURNING:
				burning = emitter();
				burning.pour( FlameParticle.FACTORY, 0.06f );
				if (visible) {
					Sample.INSTANCE.play( Assets.Sounds.BURNING );
				}
				break;
			case GODBURNING:
				red_burning = emitter();
				red_burning.pour( GodfireParticle.FACTORY, 0.02f );
				if (visible) {
					Sample.INSTANCE.play( Assets.Sounds.BURNING, 1f, 0.75f );
				}
				break;
			case SPIRIT:
				spirit = emitter();
				spirit.pour(MagicMissile.ForceParticle.FACTORY, 0.06f );
				if (visible) {
					Sample.INSTANCE.play( Assets.Sounds.BURNING );
				}
				break;
			case ALLURED:
				allured = emitter();
				allured.pour(Speck.factory(Speck.HEART), 0.08f );
				if (visible) {
					Sample.INSTANCE.play( Assets.Sounds.CHARMS );
				}
				break;
			case FROSTBURNING:
				frostburning = emitter();
				frostburning.pour( FrostfireParticle.FACTORY, 0.06f );
				if (visible) {
					Sample.INSTANCE.play( Assets.Sounds.BURNING );
				}
				break;
			case LEVITATING:
				levitation = emitter();
				levitation.pour( Speck.factory( Speck.JET ), 0.02f );
				break;
			case INVISIBLE:
				if (invisible != null) {
					invisible.killAndErase();
				}
				invisible = new AlphaTweener( this, 0.4f, 0.4f );
				if (parent != null){
					parent.add(invisible);
				} else
					alpha( 0.4f );
				break;
			case PARALYSED:
				paused = true;
				break;
			case FROZEN:
				iceBlock = IceBlock.freeze( this );
				break;
			case ILLUMINATED:
				GameScene.effect( light = new TorchHalo( this ) );
				break;
			case CHILLED:
				chilled = emitter();
				chilled.pour(SnowParticle.FACTORY, 0.1f);
				break;
			case DARKENED:
				darkBlock = DarkBlock.darken( this );
				break;
			case STONED:
				stone = StoneBlock.darken( this );
				break;
			case MARKED:
				marked = emitter();
				marked.pour(ShadowParticle.UP, 0.1f);
				break;
			case HEALING:
				healing = emitter();
				healing.pour(Speck.factory(Speck.HEALING), 0.5f);
				break;
			case SHIELDED:
				if (shield != null) {
					shield.killAndErase();
				}
				GameScene.effect(shield = new ShieldHalo(this));
				break;
			case SHRUNK:
				scale.x = 0.75f;
				scale.y = 0.75f;
				break;
			case ENLARGENED:
				scale.x = 1.5f;
				scale.y = 1.5f;
				break;
			case HEARTS:
				hearts = emitter();
				hearts.pour(Speck.factory(Speck.HEART), 0.5f);
				break;
			case WARPED:
				warped = emitter();
				warped.pour(Speck.factory(Speck.WARPCLOUD), 0.35f);
				break;
			case VINECOVERED:
				paused = true;
				vines = emitter();
				vines.start(VineParticle.FACTORY, 0.05f, 0 );
				break;
		}
	}

	public void remove( State state ) {
		switch (state) {
			case BURNING:
				if (burning != null) {
					burning.on = false;
					burning = null;
				}
				break;
			case WARPED:
				if (warped != null) {
					warped.on = false;
					warped = null;
				}
				break;
			case GODBURNING:
				if (red_burning != null) {
					red_burning.on = false;
					red_burning = null;
				}
				break;
			case SPIRIT:
				if (spirit != null) {
					spirit.on = false;
					spirit = null;
				}
				break;
			case ALLURED:
				if (allured != null) {
					allured.on = false;
					allured = null;
				}
				break;
			case FROSTBURNING:
				if (frostburning != null) {
					frostburning.on = false;
					frostburning = null;
				}
				break;
			case LEVITATING:
				if (levitation != null) {
					levitation.on = false;
					levitation = null;
				}
				break;
			case INVISIBLE:
				if (invisible != null) {
					invisible.killAndErase();
					invisible = null;
				}
				alpha( 1f );
				break;
			case PARALYSED:
				paused = false;
				break;
			case FROZEN:
				if (iceBlock != null) {
					iceBlock.melt();
					iceBlock = null;
					paused = false;
				}
				break;
			case ILLUMINATED:
				if (light != null) {
					light.putOut();
				}
				break;
			case CHILLED:
				if (chilled != null){
					chilled.on = false;
					chilled = null;
				}
				break;
			case DARKENED:
				if (darkBlock != null) {
					darkBlock.lighten();
					darkBlock = null;
				}
				break;
			case STONED:
				if (stone != null) {
					stone.lighten();
					stone = null;
				}
				break;
			case MARKED:
				if (marked != null){
					marked.on = false;
					marked = null;
				}
				break;
			case HEALING:
				if (healing != null){
					healing.on = false;
					healing = null;
				}
				break;
			case SHIELDED:
				if (shield != null){
					shield.putOut();
				}
				break;
			case SHRUNK:
			case ENLARGENED:
				scale.x = 1f;
				scale.y = 1f;
				break;
			case SWORDS:
				if (swords != null) {
					swords.on = false;
					swords = null;
				}
				break;
			case HEARTS:
				if (hearts != null){
					hearts.on = false;
					hearts = null;
				}
				break;
			case VINECOVERED:
				paused = false;
				if (vines != null) {
					vines.on = false;
					vines = null;
				}
				break;
		}
	}

	public void aura( int color ){
		if (aura != null){
			aura.killAndErase();
		}
		float size = Math.max(width(), height());
		size = Math.max(size+4, 16);
		aura = new Flare(5, size);
		aura.angularSpeed = 90;
		aura.color(color, true);
		aura.visible = visible;

		if (parent != null) {
			aura.show(this, 0);
		}
	}

	public void clearAura(){
		if (aura != null){
			aura.killAndErase();
			aura = null;
		}
	}
	
	@Override
	public void update() {
		if (paused && !looping() && ch != null && curAnim != null){
			Animation cur = curAnim;
			curAnim = null;
			listener.onComplete(cur);
		}

		super.update();

		if (flashTime > 0 && (flashTime -= Game.elapsed) <= 0) {
			resetColor();
		}

		if (burning != null) {
			burning.visible = visible;
		}
		if (warped != null) {
			warped.visible = visible;
		}
		if (spirit != null) {
			spirit.visible = visible;
		}
		if (frostburning != null) {
			frostburning.visible = visible;
		}
		if (levitation != null) {
			levitation.visible = visible;
		}
		if (iceBlock != null) {
			iceBlock.visible = visible;
		}
		if (chilled != null) {
			chilled.visible = visible;
		}
		if (marked != null) {
			marked.visible = visible;
		}
		if (healing != null){
			healing.visible = visible;
		}
		if (hearts != null){
			hearts.visible = visible;
		}
		if (vines != null){
			vines.visible = visible;
		}
		if (aura != null){
			if (aura.parent == null){
				aura.show(this, 0);
			}
			aura.visible = visible;
			aura.point(center());
		}
		if (sleeping) {
			if (!(ch instanceof Phantom))
				showSleep();
		} else {
			hideSleep();
		}
		synchronized (EmoIcon.class) {
			if (emo != null && emo.alive) {
				emo.visible = visible;
			}
		}
		if (ch != null){
			for (ChampionEnemy buff : ch.buffs(ChampionEnemy.class)) {
				hardlight(buff.color);
			}
		}
	}

	@Override
	public void resetColor() {
		super.resetColor();
		if (invisible != null){
			alpha(0.4f);
		}
	}

	public void showSleep() {
		synchronized (EmoIcon.class) {
			if (!(emo instanceof EmoIcon.Sleep)) {
				if (emo != null) {
					emo.killAndErase();
				}
				emo = new EmoIcon.Sleep(this);
				emo.visible = visible;
			}
		}
		idle();
	}

	public void hideSleep() {
		synchronized (EmoIcon.class) {
			if (emo instanceof EmoIcon.Sleep) {
				emo.killAndErase();
				emo = null;
			}
		}
	}

	public void showAlert() {
		synchronized (EmoIcon.class) {
			if (!(emo instanceof EmoIcon.Alert)) {
				if (emo != null) {
					emo.killAndErase();
				}
				emo = new EmoIcon.Alert(this);
				emo.visible = visible;
			}
		}
	}

	public void hideAlert() {
		synchronized (EmoIcon.class) {
			if (emo instanceof EmoIcon.Alert) {
				emo.killAndErase();
				emo = null;
			}
		}
	}

	public void showLost() {
		synchronized (EmoIcon.class) {
			if (!(emo instanceof EmoIcon.Lost)) {
				if (emo != null) {
					emo.killAndErase();
				}
				emo = new EmoIcon.Lost(this);
				emo.visible = visible;
			}
		}
	}

	public void hideLost() {
		synchronized (EmoIcon.class) {
			if (emo instanceof EmoIcon.Lost) {
				emo.killAndErase();
				emo = null;
			}
		}
	}

	public void hideEmo(){
		synchronized (EmoIcon.class) {
			if (emo != null) {
				emo.killAndErase();
				emo = null;
			}
		}
	}

	@Override
	public void kill() {
		super.kill();

		hideEmo();

		for( State s : State.values()){
			remove(s);
		}

		if (health != null){
			health.killAndErase();
		}
	}

	private float[] shadowMatrix = new float[16];

	@Override
	protected void updateMatrix() {
		super.updateMatrix();
		Matrix.copy(matrix, shadowMatrix);
		Matrix.translate(shadowMatrix,
				(width * (1f - shadowWidth)) / 2f,
				(height * (1f - shadowHeight)) + shadowOffset);
		Matrix.scale(shadowMatrix, shadowWidth, shadowHeight);
	}

	@Override
	public void draw() {
		if (texture == null || (!dirty && buffer == null))
			return;

		if (renderShadow) {
			if (dirty) {
				((Buffer)verticesBuffer).position(0);
				verticesBuffer.put(vertices);
				if (buffer == null)
					buffer = new Vertexbuffer(verticesBuffer);
				else
					buffer.updateVertices(verticesBuffer);
				dirty = false;
			}

			NoosaScript script = script();

			texture.bind();

			script.camera(camera());

			updateMatrix();

			script.uModel.valueM4(shadowMatrix);
			script.lighting(
					0, 0, 0, am * .6f,
					0, 0, 0, aa * .6f);

			script.drawQuad(buffer);
		}

		super.draw();

	}

	@Override
	public void onComplete( Tweener tweener ) {
		if (tweener == jumpTweener) {

			if (visible && ch.isWet() && !ch.flying) {
				GameScene.ripple( ch.pos );
			}
			if (jumpCallback != null) {
				jumpCallback.call();
			}
			GameScene.sortMobSprites();

		} else if (tweener == motion) {

			synchronized (this) {
				isMoving = false;

				motion.killAndErase();
				motion = null;
				ch.onMotionComplete();

				GameScene.sortMobSprites();
				notifyAll();
			}

		}
	}

	@Override
	public synchronized void onComplete( Animation anim ) {
		if (animCallback != null) {
			Callback executing = animCallback;
			animCallback = null;
			executing.call();
		} else {

			if (anim == attack) {

				idle();
				ch.onAttackComplete();

			} else if (anim == operate) {

				idle();
				ch.onOperateComplete();

			}

		}
	}

	private static class JumpTweener extends Tweener {

		public CharSprite visual;

		public PointF start;
		public PointF end;

		public float height;

		public JumpTweener( CharSprite visual, PointF pos, float height, float time ) {
			super( visual, time );

			this.visual = visual;
			start = visual.point();
			end = pos;

			this.height = height;
		}

		@Override
		protected void updateValues( float progress ) {
			float hVal = -height * 4 * progress * (1 - progress);
			visual.point( PointF.inter( start, end, progress ).offset( 0, hVal ) );
			visual.shadowOffset = 0.25f - hVal*0.8f;
		}
	}
}