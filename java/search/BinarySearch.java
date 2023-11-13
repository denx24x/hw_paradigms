package search;

// declare: n = arr.length
//
public class BinarySearch {
    // Pred: n > 0 && for i in 1 .. n - 1 arr[i - 1] >= arr[i] && 0 <= l < r < n && (for i in 0 .. l arr[i] > value) && (for i in r .. n - 1 arr[i] <= value)
    // Post: R : 0 <= R <= n && (for i in 0 .. R - 1 arr[i] > value) && (R == n || arr[R] <= value)
    public static int findRecursive(int[] arr, int value, int l, int r){
        if(r - l <= 1){
            // !Exists m: (l < m < r) => (r = l + 1 && (for i in 0 .. l arr[i] > value) && (for i in r .. n - 1 arr[i] <= value))  => (R = r)
            return r;
        }
        // (r - l > 1) => (Exists m : l < m < r)
        int m = l + (r - l) / 2;
        // n > 0 && for i in 1 .. n - 1 arr[i - 1] >= arr[i] && 0 <= l < r < n && (for i in 0 .. l arr[i] > value) && (for i in r .. n - 1 arr[i] <= value) && l < m < r
        if(arr[m] <= value){
            // (arr[m] <= value) => (for i in m .. n - 1 arr[i] <= value)
            // n > 0 && for i in 1 .. n - 1 arr[i - 1] >= arr[i] && 0 <= l < r < n && (for i in 0 .. l arr[i] > value) && (for i in m .. n - 1 arr[i] <= value) && m < r
            return findRecursive(arr, value, l, m);
        }else{
            // (arr[m] > value) => (for i in 0 .. m arr[i] > value)
            // n > 0 && for i in 1 .. n - 1 arr[i - 1] >= arr[i] && 0 <= l < r < n && (for i in 0 .. m arr[i] > value) && (for i in r .. n - 1 arr[i] <= value) && l < m
            return findRecursive(arr, value, m, r);
        }
    }
    // Pred: n > 0 && for i in 1 .. n - 1 arr[i - 1] >= arr[i] && arr[0] > value && arr[n - 1] <= value
    // Post: R : 0 <= R <= n && (for i in 0 .. R - 1 arr[i] > value) && (R == n || arr[R] <= value)
    public static int findIterative(int[] arr, int value){
        int l = 0;
        int r = arr.length - 1;
        // n > 0 && (for i in 1 .. n - 1 arr[i - 1] >= arr[i]) && (0 <= l` < r` < n) && (for i in 0 .. l` arr[i] > value) && (for i in r` .. n - 1 arr[i] <= value)
        while(r - l > 1){
            // (r` - l` > 1) => (Exists m : l` < m < r`)
            int m = l + (r - l) / 2;
            // n > 0 && (for i in 1 .. n - 1 arr[i - 1] >= arr[i]) && (0 <= l` < r` < n) && (for i in 0 .. l` arr[i] > value) && \
            // (for i in r` .. n - 1 arr[i] <= value) && (l` < m < r`)
            if(arr[m] > value){
                // (arr[m] > value) => (for i in 0 .. m arr[i] > value)
                l = m;
                // n > 0 && (for i in 1 .. n - 1 arr[i - 1] >= arr[i]) && (0 <= l` < r` < n) && (for i in 0 .. l` arr[i] > value) && (for i in r` .. n - 1 arr[i] <= value)
            }else{
                // (arr[m] <= value) => (for i in m .. n - 1 arr[i] <= value)
                r = m;
                // n > 0 && (for i in 1 .. n - 1 arr[i - 1] >= arr[i]) && (0 <= l` < r` < n) && (for i in 0 .. l` arr[i] > value) && (for i in r` .. n - 1 arr[i] <= value)
            }
        }
        // (!Exists m : l` < m < r`) => ((r` = l` + 1) && for i in 1 .. n - 1 arr[i - 1] >= arr[i] && \
        // (0 <= l` < r` < n) && (for i in 0 .. l` arr[i] > value) && (for i in r` .. n - 1 arr[i] <= value)) => R = r`
        return r;
    }

    // Pred: args.length > 0 && for i in 1 .. n - 1 args[i] >= args[i + 1] && args[1] > args[0] && args[n] <= args[0]
    // Post: R:  0 <= R <= n && (for i in 0 .. R - 1 arr[i + 1] > value) && (R == n || arr[R + 1] <= value)
    public static void main(String args[]){
        int value = Integer.parseInt(args[0]);
        int n = args.length - 1;
        // n >= 0
        if(n == 0){
            System.out.println(0);
            return;
        }
        // n > 0
        int[] arr = new int[n];
        for(int i = 1; i < 1 + n;i++){
            arr[i - 1] = Integer.parseInt(args[i]);
        }  
        // n > 0 && for i in 1 .. n - 1 arr[i - 1] >= arr[i]
        if(arr[0] <= value){
            // arr[0] <= value => for i in 0 .. n arr[i] <= value
            System.out.println(0);
        }else if(arr[n - 1] > value){
            // arr[n - 1] > value => for i in 0 .. n arr[i] > value
            System.out.println(n);
        }else{
            
            // n > 0 && for i in 1 .. n - 1 arr[i - 1] >= arr[i] && arr[0] > value && arr[n - 1] <= value
            System.out.println(findIterative(arr, value));
            
            
            // System.out.println(findRecursive(arr, value, 0, n - 1));
            // arr[0] > value && arr[n - 1] <= value =>
            // n > 0 && for i in 1 .. n - 1 arr[i - 1] >= arr[i] && 0 <= l < r < n && (for i in 0 .. l arr[i] > value) && (for i in r .. n - 1 arr[i] <= value)
        }
    }
}
