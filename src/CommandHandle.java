class CommandHandle{
	String cmd;
	
	String []cmdList={"search", "join","le","ls","get","pre"};
	
	
	public CommandHandle(String cmd){
		this.cmd=cmd;
	}
	
	/***
	 * if cmd is legal command
	 * @return
	 */
	public boolean isGoodCommand(){
		return true;
	}
	
	public void handle(){
		if (!isGoodCommand())
			falseCommand();
		else{
			Thread cmdThread=null;
			if (cmd.startsWith("get"))
				cmdThread=new Get();
			else if (cmd.startsWith("ls"))
				cmdThread=new LS();
			else if (cmd.startsWith("search"))
				cmdThread=new Search();
			else if (cmd.startsWith("join"))
				cmdThread=new Join();
			else if (cmd.startsWith("le"))
				cmdThread=new Leave();
			else
				System.out.println("something bad happens");
		}
		
	}
	/***
	 * this command is not legal, print out some error information
	 */
	private void falseCommand(){
		
		
	}
	
	
	class Join extends Thread{
		@Override
		public void run(){
			
		}
		
	}
	
	class Leave extends Thread{
		@Override
		public void run(){
			
		}
	}
	
	class Search extends Thread{
		@Override
		public void run(){
			
		}
	}
	
	class Get extends Thread{
		@Override
		public void run(){
			
		}
	}
	
	class LS extends Thread{
		@Override
		public void run(){
			
		}
	}
	
}