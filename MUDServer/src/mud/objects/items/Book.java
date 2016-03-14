package mud.objects.items;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.interfaces.Editable;
import mud.misc.SlotTypes;
import mud.objects.Item;
import mud.objects.ItemTypes;
import mud.utils.Utils;

/* genericized or not? */


public class Book extends Item implements Editable {

	private String title = "";
	private String author = "";
	private ArrayList<List<String>> pages;
	
	private Integer currentPage = 0;
	
	public Book() {
		this("", "", 0);
	}
	
	public Book(String bookTitle) {
		this(bookTitle, "", 0);
	}

	public Book(String bookTitle, String bookAuthor) {
		this(bookTitle, bookAuthor, 0);
	}

	public Book(String bookTitle, String bookAuthor, int pages) {
		super(-1);
		
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		
		this.type = TypeFlag.ITEM;
		this.item_type = ItemTypes.BOOK;
		
		this.equip_type = ItemTypes.NONE;
		
		this.title = bookTitle;
		this.author = bookAuthor;
		
		this.pages = new ArrayList<List<String>>(pages);
	}
	
	// TODO make sure this properly duplicates the template, which it doesn't atm
	public Book( Book template ) {
		this(template.title, template.author, template.pages.size());
		//this.item_type = template.item_type;
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other constructors
	 * that has parameters.
	 *
	 * 
	 * @param tempName
	 * @param tempDesc
	 * @param tempLoc
	 * @param tempDBREF
	 * @param tCharges
	 * @param spellName
	 */
	public Book(String tempName, String tempDesc, int tempLoc, int tempDBREF) {
		super(tempDBREF, tempName, EnumSet.noneOf(ObjectFlag.class), tempDesc, tempLoc);
		
		this.type = TypeFlag.ITEM;
		this.item_type = ItemTypes.BOOK;
		this.slot_type = SlotTypes.NONE;
		
		this.pages = new ArrayList<List<String>>(0);
	}

	@Override
	public String read() {
		return this.getPage(currentPage).toString();
	}

	@Override
	public void write(String text) {
		getPage(currentPage).add(text);
	}
	
	public void addPage(Integer newPageNum, List<String> list) {
		this.pages.add(list);
	}
	public List<String> getPage(int pageNum) {
		return this.pages.get(pageNum);
	}

	public List<String> setPage(Integer newPageNum, List<String> list) {
		if( this.pages.get(newPageNum) != null ) {
			return this.pages.set(newPageNum, list);
		}
		else {
			this.pages.add(list);
			return null;
		}
	}
	
	public Integer getPageNum() {
		return this.currentPage;
	}

	public void setPageNum(Integer newPageNum) {
		this.currentPage = newPageNum;
	}
	
	public String getAuthor() {
		return this.author;
	}
	
	public void setAuthor(String newAuthor) {
		this.author = newAuthor;
	}

	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String newTitle) {
		this.title = newTitle;
	}

	/* should increase the size of the array, but needs to preserve
	 * existing content and it's location in relation to the whole
	 */
	public void setSize(int newSize) {
		this.pages = new ArrayList<List<String>>(newSize);
	}

	public int getSize() {
		return this.pages.size();
	}
	
	public void turnToPage(int newPage) {
		this.currentPage = newPage;
	}
	
	public List<String> nextPage() {
		this.currentPage++;
		return getPage(this.currentPage);
	}
	
	public List<String> prevPage() {
		this.currentPage--;
		return getPage(this.currentPage);
	}
	
	public ArrayList<String> look() {
		return null;
	}
	
	@Override
	public String getName() {
		return getTitle();
	}

	@Override
	public String toDB() {
		String[] output = new String[11];
		
		output[0] = this.getDBRef() + "";              // database reference number
		output[1] = this.getName();                    // name
		output[2] = TypeFlag.asLetter(this.type) + ""; // flags
		output[2] = output[2] + getFlagsAsString();
		output[3] = this.getDesc();                    // description
		output[4] = this.getLocation() + "";           // location
		
		output[5] = this.item_type.getId() + "";       // item type
		output[6] = this.equip_type.getId() + "";      // equip type
		output[7] = this.slot_type.getId() + "";       // slot type
		
		output[8] = this.getAuthor();                  // book author
		output[9] = this.getTitle();                   // book title
		output[10] = this.pages.size() + "";           // # of pages
		
		return Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
}