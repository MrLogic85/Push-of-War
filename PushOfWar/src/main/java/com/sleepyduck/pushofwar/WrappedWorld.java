package com.sleepyduck.pushofwar;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.broadphase.BroadPhaseStrategy;
import org.jbox2d.collision.broadphase.DynamicTree;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.PulleyJoint;
import org.jbox2d.pooling.IWorldPool;
import org.jbox2d.pooling.arrays.Vec2Array;
import org.jbox2d.pooling.normal.DefaultWorldPool;

import com.sleepyduck.pushofwar.util.ColorChooser;

public abstract class WrappedWorld extends World {
	public static Integer LIQUID_INT = new Integer(1234598372);
	private float liquidLength = .12f;
	private float averageLinearVel = -1;
	private final Vec2 liquidOffset = new Vec2();
	private final Vec2 circCenterMoved = new Vec2();
	private final Color3f liquidColor = new Color3f(.4f, .4f, 1f);

	private DebugDraw m_debugDraw;
	private IWorldPool pool;

	private final Color3f color = new Color3f();
	private final Transform xf = new Transform();
	private final Vec2 cA = new Vec2();
	private final Vec2 cB = new Vec2();
	private final Vec2Array avs = new Vec2Array();

	private final Vec2 center = new Vec2();
	private final Vec2 axis = new Vec2();
	private final Vec2 v1 = new Vec2();
	private final Vec2 v2 = new Vec2();
	private final Vec2Array tlvertices = new Vec2Array();

	public WrappedWorld(Vec2 gravity, IWorldPool argPool) {
		this(gravity, argPool, new DynamicTree());
	}

	public WrappedWorld(Vec2 gravity) {
		this(gravity, new DefaultWorldPool(WORLD_POOL_SIZE,
				WORLD_POOL_CONTAINER_SIZE));
	}

	public WrappedWorld(Vec2 gravity, IWorldPool argPool,
			BroadPhaseStrategy broadPhaseStrategy) {
		super(gravity, argPool, broadPhaseStrategy);
		pool = argPool;
	}

	public void drawDebugData() {
		if (m_debugDraw == null) {
			return;
		}

		int flags = m_debugDraw.getFlags();

		if ((flags & DebugDraw.e_shapeBit) == DebugDraw.e_shapeBit) {
			for (Body b = getBodyList(); b != null; b = b.getNext()) {
				xf.set(b.getTransform());
				for (Fixture f = b.getFixtureList(); f != null; f = f.getNext()) {
					if (f.getUserData() instanceof ColorChooser)
						((ColorChooser) f.getUserData()).setColor(b, color);
					else if (b.isActive() == false)
						color.set(0.5f, 0.5f, 0.3f);
					else if (b.getType() == BodyType.STATIC)
						color.set(0.5f, 0.9f, 0.3f);
					else if (b.getType() == BodyType.KINEMATIC)
						color.set(0.5f, 0.5f, 0.9f);
					else if (b.isAwake() == false)
						color.set(0.5f, 0.5f, 0.5f);
					else
						color.set(0.9f, 0.7f, 0.7f);
					drawShape(f, xf, color);
				}
			}
		}

		if ((flags & DebugDraw.e_jointBit) == DebugDraw.e_jointBit) {
			for (Joint j = getJointList(); j != null; j = j.getNext()) {
				drawJoint(j);
			}
		}

		if ((flags & DebugDraw.e_pairBit) == DebugDraw.e_pairBit) {
			color.set(0.3f, 0.9f, 0.9f);
			for (Contact c = m_contactManager.m_contactList; c != null; c = c
					.getNext()) {
				Fixture fixtureA = c.getFixtureA();
				Fixture fixtureB = c.getFixtureB();
				fixtureA.getAABB(c.getChildIndexA()).getCenterToOut(cA);
				fixtureB.getAABB(c.getChildIndexB()).getCenterToOut(cB);
				m_debugDraw.drawSegment(cA, cB, color);
			}
		}

		if ((flags & DebugDraw.e_aabbBit) == DebugDraw.e_aabbBit) {
			color.set(0.9f, 0.3f, 0.9f);

			for (Body b = getBodyList(); b != null; b = b.getNext()) {
				if (b.isActive() == false) {
					continue;
				}

				for (Fixture f = b.getFixtureList(); f != null; f = f.getNext()) {
					for (int i = 0; i < f.m_proxyCount; ++i) {
						AABB aabb = m_contactManager.m_broadPhase
								.getFatAABB(0/* proxy.proxyId */); // TODO
						Vec2[] vs = avs.get(4);
						vs[0].set(aabb.lowerBound.x, aabb.lowerBound.y);
						vs[1].set(aabb.upperBound.x, aabb.lowerBound.y);
						vs[2].set(aabb.upperBound.x, aabb.upperBound.y);
						vs[3].set(aabb.lowerBound.x, aabb.upperBound.y);
						m_debugDraw.drawPolygon(vs, 4, color);
					}
				}
			}
		}

		if ((flags & DebugDraw.e_centerOfMassBit) == DebugDraw.e_centerOfMassBit) {
			for (Body b = getBodyList(); b != null; b = b.getNext()) {
				xf.set(b.getTransform());
				xf.p.set(b.getWorldCenter());
				m_debugDraw.drawTransform(xf);
			}
		}

		if ((flags & DebugDraw.e_dynamicTreeBit) == DebugDraw.e_dynamicTreeBit) {
			m_contactManager.m_broadPhase.drawTree(m_debugDraw);
		}
	}

	private void drawShape(Fixture fixture, Transform xf, Color3f color) {
		switch (fixture.getType()) {
		case CIRCLE: {
			CircleShape circle = (CircleShape) fixture.getShape();

			// Vec2 center = Mul(xf, circle.m_p);
			Transform.mulToOutUnsafe(xf, circle.m_p, center);
			float radius = circle.m_radius;
			xf.q.getXAxis(axis);

			if (fixture.getUserData() != null
					&& fixture.getUserData().equals(LIQUID_INT)) {
				Body b = fixture.getBody();
				liquidOffset.set(b.m_linearVelocity);
				float linVelLength = b.m_linearVelocity.length();
				if (averageLinearVel == -1) {
					averageLinearVel = linVelLength;
				} else {
					averageLinearVel = .98f * averageLinearVel + .02f
							* linVelLength;
				}
				liquidOffset.mulLocal(liquidLength / averageLinearVel / 2);
				circCenterMoved.set(center).addLocal(liquidOffset);
				center.subLocal(liquidOffset);
				m_debugDraw.drawSegment(center, circCenterMoved, liquidColor);
				return;
			}

			m_debugDraw.drawSolidCircle(center, radius, axis, color);
		}
			break;

		case POLYGON: {
			PolygonShape poly = (PolygonShape) fixture.getShape();
			int vertexCount = poly.m_count;
			assert (vertexCount <= Settings.maxPolygonVertices);
			Vec2[] vertices = tlvertices.get(Settings.maxPolygonVertices);

			for (int i = 0; i < vertexCount; ++i) {
				// vertices[i] = Mul(xf, poly.m_vertices[i]);
				Transform.mulToOutUnsafe(xf, poly.m_vertices[i], vertices[i]);
			}

			m_debugDraw.drawSolidPolygon(vertices, vertexCount, color);
		}
			break;
		case EDGE: {
			EdgeShape edge = (EdgeShape) fixture.getShape();
			Transform.mulToOutUnsafe(xf, edge.m_vertex1, v1);
			Transform.mulToOutUnsafe(xf, edge.m_vertex2, v2);
			m_debugDraw.drawSegment(v1, v2, color);
		}
			break;

		case CHAIN: {
			ChainShape chain = (ChainShape) fixture.getShape();
			int count = chain.m_count;
			Vec2[] vertices = chain.m_vertices;

			Transform.mulToOutUnsafe(xf, vertices[0], v1);
			for (int i = 1; i < count; ++i) {
				Transform.mulToOutUnsafe(xf, vertices[i], v2);
				m_debugDraw.drawSegment(v1, v2, color);
				m_debugDraw.drawCircle(v1, 0.05f, color);
				v1.set(v2);
			}
		}
			break;
		default:
			break;
		}
	}

	private void drawJoint(Joint joint) {
		Body bodyA = joint.getBodyA();
		Body bodyB = joint.getBodyB();
		Transform xf1 = bodyA.getTransform();
		Transform xf2 = bodyB.getTransform();
		Vec2 x1 = xf1.p;
		Vec2 x2 = xf2.p;
		Vec2 p1 = pool.popVec2();
		Vec2 p2 = pool.popVec2();
		joint.getAnchorA(p1);
		joint.getAnchorB(p2);

		color.set(0.5f, 0.8f, 0.8f);

		switch (joint.getType()) {
		case DISTANCE:
			m_debugDraw.drawSegment(p1, p2, color);
			break;

		case PULLEY: {
			PulleyJoint pulley = (PulleyJoint) joint;
			Vec2 s1 = pulley.getGroundAnchorA();
			Vec2 s2 = pulley.getGroundAnchorB();
			m_debugDraw.drawSegment(s1, p1, color);
			m_debugDraw.drawSegment(s2, p2, color);
			m_debugDraw.drawSegment(s1, s2, color);
		}
			break;
		case CONSTANT_VOLUME:
		case MOUSE:
			// don't draw this
			break;
		default:
			m_debugDraw.drawSegment(x1, p1, color);
			m_debugDraw.drawSegment(p1, p2, color);
			m_debugDraw.drawSegment(x2, p2, color);
		}
		pool.pushVec2(2);
	}

	public void setDebugDraw(DebugDraw debugDraw) {
		super.setDebugDraw(debugDraw);
		m_debugDraw = debugDraw;
	}
}
