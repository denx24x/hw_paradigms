package search;

// declare: n = arr.length
//
public class BinarySearchMin {

    // Pred: n > 1 && Exists M : (for i in 1 .. M arr[i] < arr[i - 1] && for i in M + 1 .. n - 2 arr[i] < arr[i + 1])
    //          && 0 <= l < r < n && l <= M <= r
    // Post: R = min(arr)
    public static int findRecursive(final int[] arr, final int l, final int r) {
        if (r - l <= 1) {
            // :NOTE: Почему?
            // (!Exist m: l < m < r) => (r = l + 1) & (l <= M <= r) => min(arr) = min(arr[l], arr[r])
            return Math.min(arr[l], arr[r]);
        }

        // r - l > 1 => Exists m : l < m < r
        final int m = l + (r - l) / 2;
        // n > 1 && l >= 0 && r < n && l < r && Exists M : (for i in 1 .. M arr[i] < arr[i - 1] && for i in M + 1 .. n - 2 arr[i] < arr[i + 1]) && l <= M <= r && l < m < r
        if (arr[m - 1] <= arr[m] && arr[m] <= arr[m + 1]) {
            // :NOTE: Какое M?
            // (arr[m - 1] <= arr[m] && arr[m] <= arr[m + 1]) => (arr[r - 1] <= arr[r] && arr[r] <= arr[r + 1]) => (M <= m)
            // n > 1 && Exists M : (for i in 1 .. M arr[i] < arr[i - 1] && for i in M + 1 .. n - 2 arr[i] < arr[i + 1]) && l >= 0 && m < n && l < m && l <= M <= m
            return findRecursive(arr, l, m);
        } else {
            // (arr[m - 1] > arr[m] || arr[m] > arr[m + 1]) 
            // (arr[m] > arr[m + 1]) => (arr[m - 1] > arr[m]) =>
            //  (arr[l - 1] > arr[l] > arr[l + 1]) => (M >= l)
            // n > 1 && Exists M : (for i in 1 .. M arr[i] < arr[i - 1] && for i in M + 1 .. n - 2 arr[i] < arr[i + 1]) && m >= 0 && r < n && m < r && m <= M <= r
            return findRecursive(arr, m, r);
        }
    }

    // Pred: n > 1 && Exists M : (for i in 1 .. M arr[i] < arr[i - 1] && for i in M + 1 .. n - 2 arr[i] < arr[i + 1]) 
    // Post: R = min(arr)
    public static int findIterative(final int[] arr){
        int l = 0;
        int r = arr.length - 1;
        // n > 1 && l >= 0 && r < n && l < r && Exists M : (for i in 1 .. M arr[i] < arr[i - 1] && for i in M + 1 .. n - 2 arr[i] < arr[i + 1]) && l <= M <= r
        while(r - l > 1){
            // r - l > 1 => Exists m : l < m < r
            final int m = l + (r - l) / 2;
            // n > 1 && l >= 0 && r < n && l < r && Exists M : (for i in 1 .. M arr[i] < arr[i - 1] && for i in M + 1 .. n - 2 arr[i] < arr[i + 1]) && l <= M <= r && l < m < r
            if(arr[m - 1] <= arr[m] && arr[m] <= arr[m + 1]){
                // (arr[m - 1] <= arr[m] && arr[m] <= arr[m + 1]) => (arr[r - 1] <= arr[r] && arr[r] <= arr[r + 1]) => (M <= m)
                r = m;
            }else{
                // (arr[m - 1] > arr[m] || arr[m] > arr[m + 1]) 
                // (arr[m] > arr[m + 1]) => (arr[m - 1] > arr[m]) =>
                //  (arr[l - 1] > arr[l] > arr[l + 1]) => (M >= l)
                l = m;
            }
        }
        // n > 1 && l >= 0 && r < n && l < r && Exists M : (for i in 1 .. M arr[i] < arr[i - 1] && for i in M + 1 .. n - 2 arr[i] < arr[i + 1]) && l <= M <= r
        // (!Exist m: l < m < r) => (r = l + 1) & (l <= M <= r) => min(arr) = min(arr[l], arr[r])
        return Math.min(arr[l], arr[r]);
    }
    
    // Pred: args.length > 0 && Exists m : (for i in 1 .. m: args[i] < args[i - 1] && for i in m + 1 .. n - 2 args[i] < args[i + 1])
    // :NOTE: min(args)
    // Post: R = min(args)
    public static void main(final String[] args){
        final int n = args.length;
        final int[] arr = new int[n];
        for(int i = 0; i < n;i++){
            arr[i] = Integer.parseInt(args[i]);
        }
        // n > 0 && Exists m : (for i in 1 .. m arr[i] < arr[i - 1] && for i in m + 1 .. n - 2 arr[i] < arr[i + 1])
        if(n == 1){
            // n == 1 => arr[0] = min(arr)
            System.out.println(arr[0]);
        }else{
            // n > 1 && Exists m : (for i in 1 .. m arr[i] < arr[i - 1] && for i in m + 1 .. n - 2 arr[i] < arr[i + 1]) 
            System.out.println(findIterative(arr));

            // n > 1 && Exists M : (for i in 1 .. M arr[i] < arr[i - 1] && for i in M + 1 .. n - 2 arr[i] < arr[i + 1]) && l >= 0 && r < n && l < r && l <= M <= r
            //System.out.println(findRecursive(arr, 0, n - 1));
        }
    }
}
