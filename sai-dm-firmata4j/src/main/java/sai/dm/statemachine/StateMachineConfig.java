package sai.dm.statemachine;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

	@Autowired Actions actions;
	@Autowired Guards guards;

	@Override
	public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
		config
		.withConfiguration()
			.autoStartup(true)
			.listener(listener());
	}

	@Bean
	public StateMachineListener<States, Events> listener() {
		return new StateMachineListenerAdapter<States, Events>() {

			@Override
			public void stateChanged(State<States, Events> from, State<States, Events> to) {
				log.info("State change to " + to.getId());
			}
		};
	}

	@Override
	public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
		states
		.withStates()
			.initial(States.IDLE)
			.states(EnumSet.allOf(States.class));
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
		transitions
		.withExternal()
			.source(States.IDLE).target(States.CONNECTING).event(Events.CONNECT)
			.action(actions.idleConnect())
			.and()
		.withExternal()
			.source(States.CONNECTING).target(States.IDLE).event(Events.DISCONNECT)
			.action(actions.disconnect())
			.and()
		.withExternal()
			.source(States.CONNECTING).target(States.CONNECTED).timer(5000)
			.guard(guards.connected())
			.and()
		.withExternal()
			.source(States.CONNECTED).target(States.IDLE).event(Events.DISCONNECT)
			.action(actions.disconnect())
			.and()
		.withExternal()
			.source(States.CONNECTED).target(States.CONNECTING).event(Events.RESET)
			.action(actions.reset())
			.and()
		.withExternal()
			.source(States.CONNECTING).target(States.PORT_MISSING).event(Events.PORTS_CHANGED)
			.guard(guards.connectChanged())
			.and()
		.withExternal()
			.source(States.CONNECTED).target(States.PORT_MISSING).event(Events.PORTS_CHANGED)
			.guard(guards.connectChanged())
			.and()
		.withExternal()
			.source(States.PORT_MISSING).target(States.CONNECTING).event(Events.PORTS_CHANGED)
			.guard(guards.portMissingChanged())
			.and()
		.withInternal()
			.source(States.CONNECTED).event(Events.CONNECTED);
	}

}
