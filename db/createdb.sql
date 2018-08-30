-- The calculation id for identfying current session

create sequence calculation_id_seq;

-- The sentences table contains all sentences from the corpus
--  + Document ID		-> the id of the Document for this sentence
--  + Sentence ID		-> the id of the Sentence
--  + Text			-> the text of the sentence
create table sentences (
	doc_id integer,
	sentence_id integer,
	text text,
	primary key (doc_id, sentence_id)
);

-- The words table contains all words from the corpus and the information about them
--  + Word ID			-> the unique word id
--  + word			-> the actual string for a word
--  + Document ID		-> the id of the Document for this markable
--  + Paragraph ID        -> the id of the Paragraph for this markable
--  + Sentence ID		-> the id of the Sentence for this markable
--  + Word Feature List		-> a list of features for this word
create table words (
	word_id integer primary key,
	word text,
	doc_id integer,
    paragraph_id integer,
	sentence_id integer,
	wordfeatures text[],
	foreign key (doc_id, sentence_id) references sentences
);

-- The markables table contains all markables from a corpus and the information about them
--  + Markable ID		-> the markable id
--  + First word		-> the id of the first word of the markable
--  + Last word			-> the id of the last word of the markable
--  + Head			-> the id of the head word of the markable
--  + Content			-> the actual string for a markable
--  + Document ID		-> the id of the Document for this markable
--  + Sentence ID		-> the id of the Sentence for this markable
--  + Markable Feature List	-> a list of features for a given markables 
create table markables (
	markable_id integer primary key,
	firstword integer references words,
	lastword integer references words,
	head integer references words,
	content text,
	document_id integer,
	sentence_id integer,
	markable_features text[],
   entity_id integer,
	check (firstword <= lastword),
	check (firstword <= head and head <= lastword)
);

-- The link table stores information about the links
--  + Link ID		-> the unique Link ID
--  + Markable1		-> the id of the first markable of the link
--  + Markable2		-> the id of the second markable of the link
--  + Label		-> the label (coref/disref/unknown -> +1/-1/0) for the link
--  + Confidence level	-> the level of confidence about the label [0..100]
create table links (
	link_id integer primary key,
	markable1 integer references markables,
	markable2 integer references markables,
	label integer,
	confidence float
);

-- The feature vector table contains information about feature vectors for a link
--  + Feature Vector ID	-> unique key for a feature vector
--  + Features		-> the actual feature vector
--  + Link ID 		-> the id of the link the feature vector belongs to
create table featurevectors (
	featurevector_id integer primary key,
	features float[],
	link_id integer references links
);

-- The calculations table contains information about the SOMs calculated from the feature vectors
--  + Calculation ID		-> unique key for a calculation
--  + Codebook			-> matrix of the weights of the map units
--  + Map Coordinates 		-> matrix of coordinates of the map units
--  + Map Neighbours		-> matrix of neighbourhood of the map units
--  + U-Matrix 			-> u-matrix of the som
--  + Best Matching Units	-> best matching units to all feature vectors
--  + Feature Vectors 		-> ids of the feature vectors used for the calculation
create table calculations (
   calculation_id integer primary key,
   codebook float[][],
   map_coords float[][],
   map_neighbours float[][],
   u_matrix float[][],
   bmus integer[],   
   featurevector_ids integer[]
);