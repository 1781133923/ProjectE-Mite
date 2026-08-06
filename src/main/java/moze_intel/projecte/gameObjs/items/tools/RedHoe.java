package moze_intel.projecte.gameObjs.items.tools;

public class RedHoe extends DarkHoe
{
	public RedHoe() 
	{
		super("rm_hoe", (byte)3, new String[]{});
		
		this.peToolMaterial = "rm_tools";
		this.pePrimaryToolClass = "hoe";
	}

	@Override
	public float getAttackDamage()
	{
		return 10.0F;
	}
}
