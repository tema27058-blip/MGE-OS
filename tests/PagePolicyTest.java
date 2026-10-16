import com.mna.launcher.PagePolicy;
public final class PagePolicyTest {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args){
  int count=PagePolicy.count(4,1),page=4;
  for(int n=0;n<1000;n++)page=PagePolicy.clamp(page+1,count);
  check(count==5&&page==4,"swipe created a sixth page");
  check(PagePolicy.clamp(99,5)==4,"saved index not clamped");
  check(PagePolicy.clamp(-1,5)==0,"negative index");
  check(PagePolicy.clamp(1,1)==0,"one-page boundary");
  check(PagePolicy.count(4,6)==6,"explicit page lost");
  check(PagePolicy.clamp(6,6)==5,"explicit page boundary");
  System.out.println("PASS: repeated swipes and explicit page creation");
 }
}
