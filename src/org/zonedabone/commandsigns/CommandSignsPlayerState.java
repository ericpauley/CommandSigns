package org.zonedabone.commandsigns;

public enum CommandSignsPlayerState {
	ENABLE(0),
	DISABLE(1),
	READ(2),
	COPY(3);
	
	private final int id;
	
	private CommandSignsPlayerState(final int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean equals(CommandSignsPlayerState state) {
		if(state.getId() == this.id) {
			return true;
		}
		return false;
	}

}
