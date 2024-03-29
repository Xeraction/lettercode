# Lettercode Documentation

Lettercode has to be on one line with no spaces.<br>
Below are the syntax letters with their equivalent in literally every other language.<br>
Also, every syntax has a short example with the equivalent in other languages.

### l = ;
Has to be at the end of every statement<br>
Example: hsHis**l** (print("Hi")**;**)

### x = End program
The program has to end with this, or it will not run.<br>
You can use it everywhere else as well, like inside if statements.<br>
Example: **x**l (**exit()**;)

### h = print()
Prints a value to the console<br>
Example: **hsHis**l (**print("Hi")**;)<br>
As values for variables and stuff like this, the structure (surrounder)(content)(surrounder) is used.

### Surrounders:
- s (string) Example: **s**Hi**s**
- c (character) Example: **c**H**c**
- i (integer) Example: **i**42**i**
- d (double) Example: **d**4.2**d**
- b (boolean) Example: **b**true**b**

A special case is **u** (user input):<br>
It doesn't follow the same structure, it's just **u**.<br>
It requests input from the user and automatically converts it into the appropriate type.

### Rules for string/character values:
The character 'g' is the escape character.
- gn = newline
- gl = whitespace
- gs/gc = escape characters to not end string early
- gg = escape itself

### v = Variable Initialization
Initializes a new variable.<br>
Example: vVARei42il (let var = 42;)

### Rules for variables:
Variable names must be uppercase. They can contain numbers, but the first character must be an uppercase letter.
- e = **=** (Used to assign a value to variables)<br>Example: vVAR**e**sHisl (let var **=** "Hi";)
- p = **+** (Used for addition)<br>Example: vVARei4i**p**i2il (let var = 4 **+** 2;)
- m = **-** (Used for subtraction)<br>Example: vVARei4i**m**i2il (let var = 4 **-** 2;)
- n = __\*__ (Used for multiplication)<br>Example: vVARei4i**n**i2il (let var = 4 __\*__ 2;)
- q = **/** (Used for division)<br>Example: vVARei4i**q**i2il (let var = 4 **/** 2;)
- y = **%** (Used for modulo)<br>Example: vVARei4i**y**i2il (let var = 4 **%** 2;)

Note that operator precedence does not exist. Values are always evaluated from left to right.

### Variable Modification
Start a statement with an uppercase variable name to make operations on it.
- pe = **+=** (Adds a value to a variable)<br>Example: VAR**pe**i42il (var **+=** 42;)
- me = **-=** (Subtracts a value from a variable)<br>Example: VAR**me**i42il (var **-=** 42;)
- ne = __\*=__ (Multiplies a variable with a value)<br>Example: VAR**ne**i42il (var __\*=__ 42;)
- qe = **/=** (Divides a variable by a value)<br>Example: VAR**qe**i42il (var **/=** 42;)
- ye = **%=** (Uses the modulo operation on a variable and value)<br>Example: VAR**ye**i42il (var **%=** 42;)
- pp = **++** (Shorthand for += 1)<br>Example: VAR**pp**l var<strong>++</strong>
- mm = **--** (Shorthand for += 1)<br>Example: VAR**mm**l (var<strong>--</strong>;)

### Conditions
Conditions are used in two places: if and while statements.<br>
To make a basic condition, enter a value, a conditional operator, and another value. (val1 op val2)<br>
You can then chain multiple basic conditions together using logical operators. These chains are evaluated from left to right.

### Conditional operators
- gt = **>** (greater than)
- get = **>=** (greater than or equal to)
- lt = **<** (less than)
- let = **<=** (less than or equal to)
- et = **==** (equal to)
- at = **!=** (not equal to)

Example: i42igti7i<br>
Evaluates to true, because 42 is greater than 7.<br>

### Logical operators
- a = **&&** (and)
- o = **||** (or)
- x = **^** (xor)

Example: i42igti7iai5ilti10i<br>
Evaluates to false, because the two conditions are chained with the "and" operator and the second condition is false.

### j = if
Executes a block of code when a condition is true.<br>
Structure: j(condition)t(code)z<br>
t is the opening brace ({) and z the closing brace (})<br>
Example: jVARlti42ithi42iz<br>
If the variable "var" is less than 42, it will print 42.

### e = else
After the closing "z" of an if body, there can be an else body.<br>
Structure: ...zet(code)z<br>
Example: jVARlti42ithi42izethi0iz<br>
If the variable "var" is less than 42, it will print 42. Otherwise, it will print 0.

### r = while
Executes a block of code continually while a condition is true.<br>
Structure: r(condition)t(code)z<br>
Example: rVARleti42itVARpplz<br>
Increases the variable "var" by one until it equals 42.

### f = for
Executes a block of code continually while a condition is true with options for counter variables.
Structure: f(top1)k(condition)k(top2)t(code)z<br>
top1 can only be variable initializations and/or modifications<br>
top2 can only be variable modifications<br>
Multiple instructions are separated by their ending "l"<br>
Example: fvVAR1ei1ilvVAR2ei10ilkVAR1ltVAR2kVAR1pplVAR2mmltz<br>
Increases var1 and decreases var2 while var1 is less than var2

### Scopes:
When a new code body is opened, for example with an if statement, this code body also opens
a new scope. When you define a variable inside a scope, this variable cannot be accessed outside
of it.<br>
Example: jNUMeti42itvVARei1ilzhVARl<br>
This doesn't work, because "var" was defined inside the if block and is being used outside of it.